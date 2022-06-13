package yelp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YelpAutomation {

    static WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void yelpMainPage(){
        String url = "https://www.yelp.com/";

        driver.manage().window().maximize();
        driver.get(url);

        assertEquals(driver.getTitle() , "Restaurants, Dentists, Bars, Beauty Salons, Doctors - Yelp");
    }

    @Test
    public void systemResponseWhileSearchingForFood () throws InterruptedException {

        background();

        Thread.sleep(4000);
        String pizzaPageXPATH = "//h1/span";
        WebElement pizzaPage = driver.findElement(By.xpath(pizzaPageXPATH));

        assertEquals(true , pizzaPage.getText().contains("Restaurants pizza"));

    }

    @Test
    public void totalOfSearchResults () {

        background();

        String resultsXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]/div/div/h3/span";
        try {
            List<WebElement> results = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(resultsXPATH)));
            System.out.println("Total of results: " + countResults(results));
        }catch (Exception e){
            System.out.println("Error in totalOfSearchResults");
        }

    }

    @ParameterizedTest
    @CsvSource({
            "Alcohol@Full Bar,Neighborhoods@Chinatown",
            "Parking@Street,Neighborhoods@Castro",
            "Smoking@Outdoor Area / Patio Only,Neighborhoods@Marina/Cow Hollow"
    })
    public void systemBehaviorWhenApplyingFilters(String features, String neighborhoods){
        background();

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
        }catch (StaleElementReferenceException e){

        }

    }

    @Test
    public void reportStarRating(){
        background();
        countStars();
    }

    @Test
    public void clickOnFirstRestaurant(){

        background();

        String firstResultXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]//span[text()='1']";
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(firstResultXPATH)));
        firstResult.click();

    }

    @Test
    public void criticalInformationOfRestaurant(){
        background();

        String firstResultXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]//span[text()='1']";
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(firstResultXPATH)));
        firstResult.click();


        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());

        if (tabs.size() != 1) {
            driver.switchTo().window(tabs.get(1));
        }
        System.out.println(firstResultCriticalInformation());
    }

    @Test
    public void firstThreeReviews(){
        background();

        String firstResultXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[1]//span[text()='1']";
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(firstResultXPATH)));
        firstResult.click();

        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());

        if (tabs.size() != 1) {
            driver.switchTo().window(tabs.get(1));
        }
        getFirstThreeReviews();

    }

    public void getFirstThreeReviews(){


        String reviewerNamesXPATH = "//section[@aria-label='Recommended Reviews']/div[2]/div/ul/li/div/div[1]/div/div/div/div/div[2]/div[1]/span/a";
        String reviewCommentXPATH = "//section[@aria-label='Recommended Reviews']/div[2]/div/ul/li/div/div[4]/p/span";

        List<WebElement> reviewersNames = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(reviewerNamesXPATH)));
        List<WebElement> reviewComments = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(reviewCommentXPATH)));

        int numberOfRequiredReviews = 3;
        String comments [] = new String[numberOfRequiredReviews];
        String names [] = new String[numberOfRequiredReviews];
        String mergedReviews[] = new String[numberOfRequiredReviews];

        for(int i = 0 ; i < comments.length; i++){
            comments[i] = reviewComments.get(i).getText();
        }
        for(int i = 0 ; i < comments.length; i++){
            names[i] = reviewersNames.get(i).getText();
        }

        for(int i = 0 ; i < comments.length; i++){
            mergedReviews[i] = "Name:\t" + names[i] + "\n"+ "Review:\t" + comments[i]+"\n";
        }

        for(int i = 0 ; i < comments.length; i++){
            System.out.println(mergedReviews[i]);
        }

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

    public void countStars() {

        String startsXPATH = "//*[@id=\"main-content\"]/div/ul/li/div/div/div/div[2]/div[1]/div[1]/div[2]/div/div[1]/div[1]/span/div";
        List<WebElement> auxResults = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(startsXPATH)));

        System.out.println("Rating of all results in page: ");
        for (WebElement result : auxResults) {
            System.out.print(result.getAttribute("aria-label")+"\t"+"|"+"\t");
        }
        System.out.println();

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

    private void background (){
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

    private void setFiltersInModal(String features) {

        String searchButtonXPATH = "//span[text()='Search']/..";

        String modalElements[] = features.split("@");
        String categoryString = modalElements[0].trim();
        String selectionString = modalElements[1].trim();

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

}
