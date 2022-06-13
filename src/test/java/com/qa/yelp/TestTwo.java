package com.qa.yelp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TestTwo {

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }


    public void searchForRestaurant() {

        String url = "https://www.yelp.com/";

        driver.manage().window().maximize();
        driver.get(url);


        String restaurantsDropdownXPATH = "//div/a/span[text()='Restaurants']/..";
        WebElement restaurantsDropDown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(restaurantsDropdownXPATH)));
        restaurantsDropDown.click();


        String searchInputXPATH = "//input[@name='find_desc']";
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(searchInputXPATH)));

        searchInput.sendKeys(" pizza");
        searchInput.sendKeys(Keys.ENTER);

    }

    public String firstResultCriticalInformation() {
        String name;
        String phone;
        String address;
        String website;


        WebElement elementName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1")));
        name = elementName.getText();

        WebElement elementPhone = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//aside//p[text()='Phone number']/../p[2]")));
        phone = elementPhone.getText();

        WebElement elementAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//aside//a[text()='Get Directions']//..//../p[2]")));
        address = elementAddress.getText();

        WebElement elementWebsite = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//aside//p[text()='Business website']/../p[2]")));
        website = elementWebsite.getText();


        return "Name:\t" + name + "\n" + "Phone:\t" + phone + "\n" + "Address:\t" + address + "\n" + "WebSite:\t" + website + "\n";
    }

    @ParameterizedTest
    @CsvSource({
            "Alcohol@Full Bar,Neighborhoods@Chinatown",
            "Parking@Street,Neighborhoods@Castro",
            "Smoking@Outdoor Area / Patio Only,Neighborhoods@Marina/Cow Hollow"
    })
    public void test(String features, String neighborhoods) {

        searchForRestaurant();

        String featuresModalXPATH = "//p[text()='Features']/../../div//a";
        displayModal(featuresModalXPATH);
        setFiltersInModal(features);

        String neiNeighborhoodsModalXPATH = "//p[text()='Neighborhoods']/../../div//a";
        displayModal(neiNeighborhoodsModalXPATH);
        setFiltersInModal(neighborhoods);


        String resultsXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]/div/div/h3/span";
        try {
            List<WebElement> results = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(resultsXPATH)));
            System.out.println("Total results with given filters: " + countResults(results));
            countStars();
        }catch (StaleElementReferenceException e){

        }


        String firstResultXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]//span[text()='1']";
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(firstResultXPATH)));
        firstResult.click();


        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());

        if (tabs.size() != 1) {
            driver.switchTo().window(tabs.get(1));
        }
        System.out.println(firstResultCriticalInformation());
        getFirstThreeReviews();

        //String nextPageButtonXPATH = "//a[@aria-label='Next']";
    }

    private void setFiltersInModal(String features) {

        String searchButtonXPATH = "//span[text()='Search']/..";

        String modalElems[] = features.split("@");
        String categoryString = modalElems[0].trim();
        String selectionString = modalElems[1].trim();

        String selectionXPATH;

        WebElement selection;
        WebElement searchButton;

        try {
            selectionXPATH = "//label//span[text()='" + selectionString + "']";
            selection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selectionXPATH)));
            selection.click();
            searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(searchButtonXPATH)));
            searchButton.click();

        } catch (Exception e) {

            String categoryXPATH = "//span[text()='" + categoryString + "']";
            selectionXPATH = categoryXPATH + "//..//..//../..//span[text()='" + selectionString + "']";

            WebElement category = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(categoryXPATH)));
            category.click();
            selection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(selectionXPATH)));
            selection.click();

            searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(searchButtonXPATH)));
            searchButton.click();

        }
    }

    public void displayModal(String modalXPATH) {
        WebElement displayMoreFeaturesModal = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(modalXPATH)));
        displayMoreFeaturesModal.click();
    }

    private int totalOfResults(String nextPageButtonXPATH) {

        int totalResults = 0;
        List<WebElement> results;

        while (true) {
            try {

                results = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]/div/div/h3/span")));

                totalResults = totalResults + countResults(results);

                WebElement nextPageButton = driver.findElement(By.xpath(nextPageButtonXPATH));
                nextPageButton.click();

            } catch (NoSuchElementException e) {
                break;
            } catch (StaleElementReferenceException e) {

            } catch (TimeoutException e) {
                break;
            }

        }
        return totalResults;
    }

    private int countResults(List<WebElement> results) {

        int totalResults = 0;

        for (WebElement result : results) {
            try {
                Integer.parseInt(Character.toString(result.getAttribute("innerHTML").charAt(0)));
                totalResults++;
            } catch (NumberFormatException e) {

            }
        }


        return totalResults;
    }

    public void countStars() {

        String startsXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[ ]/div[2]/div/div[1]/div[1]/span/div";
        List<WebElement> auxResults = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(startsXPATH)));

        System.out.println("Rating of all results in page: ");
        for (WebElement result : auxResults) {
            System.out.print(result.getAttribute("aria-label")+"\t"+"|"+"\t");
        }
        System.out.println();

    }

    public void getFirstThreeReviews(){


        String reviewerNamesXPATH = "//section[@aria-label='Recommended Reviews']/div[2]/div/ul/li/div/div[1]/div/div/div/div/div[2]/div[1]/span/a";
        String reviewCommentXPATH = "//section[@aria-label='Recommended Reviews']/div[2]/div/ul/li/div/div[4]/p/span";

        List<WebElement> reviewersNames = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(reviewerNamesXPATH)));
        List<WebElement> reviewComments = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(reviewCommentXPATH)));

        int numberOfRequiredReviews = 3;
        String comments [] = new String[numberOfRequiredReviews];
        String names [] = new String[numberOfRequiredReviews];
        String mergedRevies[] = new String[numberOfRequiredReviews];

        for(int i = 0 ; i < comments.length; i++){
            comments[i] = reviewComments.get(i).getText();
        }
        for(int i = 0 ; i < comments.length; i++){
            names[i] = reviewersNames.get(i).getText();
        }

        for(int i = 0 ; i < comments.length; i++){
            mergedRevies[i] = "Name:\t" + names[i] + "\n"+ "Review:\t" + comments[i]+"\n";
        }

        for(int i = 0 ; i < comments.length; i++){
            System.out.println(mergedRevies[i]);
        }

    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }
}
