import com.microsoft.playwright.CLI;

import java.io.IOException;

public class RunCodegen {
    public static void main(String[] args) throws IOException, InterruptedException {
        // You can replace the URL with your actual Jarvis UAT login page
        String[] playwrightArgs = {"codegen", "https://jarvis.uat.creditsaison.corp"};

        // This launches the Playwright Inspector and Browser
        CLI.main(playwrightArgs);
    }
}