package main.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Http {

  public void exampleUrlCall() throws MalformedURLException, IOException {

    // @@http_one
    URL github = new URL("http://www.github.com/");
    github.getContent();
    // @@http_two
    URL apache = new URL("https://www.apache.org/");
    apache.getDefaultPort();
    // @@http_two
  }
}
