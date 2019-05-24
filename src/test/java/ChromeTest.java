import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class ChromeTest {

    public static void main(String args[]) {

        try {
            //setup chrome driver
            System.setProperty("webdriver.chrome.driver", "C://chromedriver/chromedriver.exe");
            WebDriver driver = new ChromeDriver();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            //navigate to google maps, change to satellite view
            String BaseURL = "https://www.google.com/maps";
            driver.get(BaseURL);
            waitFor(4);
            driver.findElement(By.xpath("//button[@class='searchbox-hamburger']")).click();
            waitFor(2);
            driver.findElement(By.xpath("//label[@class='widget-settings-button-label'][contains(text(),'Satellite')]")).click();
            waitFor(1);

            // load all addresses from CSV into memory
            String[] addresses = getAddresses();

            String lastLoc = "lastLoc";
            String currentLoc = "currentLoc";
            int imgCount = 0;
            while (!currentLoc.equals(lastLoc)) {
                for(String location : addresses) {
                    WebElement searchBox = driver.findElement(By.cssSelector("#searchboxinput"));
                    searchBox.clear();
                    searchBox.sendKeys(location);
                    searchBox.sendKeys(Keys.RETURN);

                    //check to make sure location was properly retrieved
                    lastLoc = currentLoc;
                    currentLoc = driver.findElement(By.xpath("//*[@id=\"pane\"]/div/div[1]/div/div/div[1]/div[3]/div[1]/h1")).getText();


                    // dimension calculation
                    Rectangle view = driver.findElement(By.xpath("//div[@class='widget-scene']")).getRect();
                    Rectangle sidebar = driver.findElement(By.xpath("//div[@class='widget-pane widget-pane-visible']")).getRect();

                    view.setX(sidebar.getWidth());
                    view.setWidth(view.getWidth()-sidebar.getWidth());

                    waitFor(3);
                    getSatelliteImage(driver, view, imgCount);
                    imgCount++;

                    //get street view ?

                    //break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // creates a custom rectangle centered on map pin, with a radius of int padding. and then crops a screenshot to this
    // rectangle
    private static void getSatelliteImage(WebDriver driver, Rectangle view, int imgCount) throws Exception {

        int padding = 240;
        int width = view.getWidth();
        int height = view.getHeight();
        if(padding*2 > width || padding*2 > height){
            if(width > height){
                padding = height/2;
            } else {
                padding = width/2;
            }
        }
        view = getPadCorner(getCenter(view), padding);

        //crop and save image
        String imgDir = "C:\\Users\\Tim-Laptop\\Documents\\LiquorMaps\\liquorimg";
        ByteArrayInputStream imgbytes = new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        BufferedImage bimg = ImageIO.read(imgbytes);
        System.out.println(imgDir+imgCount+".png");
        File imgfile = new File(imgDir+imgCount+".png");
        bimg = bimg.getSubimage(view.getX(), view.getY(), view.getWidth(), view.getWidth());
        ImageIO.write(bimg, "png", imgfile);
    }

    // description
    private static void waitFor(int second){
        try{
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private static String[] getAddresses() {
        String csvFile = "C:\\Users\\Tim-Laptop\\Documents\\liquor.csv";
        BufferedReader br = null;

        String[] Addresses;

        try {
            Addresses = new String[countLines(csvFile)];
        } catch (IOException e) {
            e.printStackTrace();
            Addresses = new String[100];
        }

        try {
            br = new BufferedReader(new FileReader(csvFile));
            int count = 0;
            for (CSVRecord record : CSVFormat.DEFAULT.parse(br)) {
                if (count > 0) {
                    Addresses[count-1] = record.get(7) + ", " + record.get(8);
                }
                count++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return Addresses;
    }

    private static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    private static Point getCenter(Rectangle rect){
        int x = rect.getX() + rect.getWidth()/2;
        int y = rect.getY() + rect.getHeight()/2;
        return new Point(x, y);
    }

    private static Rectangle getPadCorner(Point center, int pad){
        int x = center.getX()-pad;
        int y = center.getY()-pad;
        int width = pad*2;
        return new Rectangle(x, y, width, width);
    }
}




