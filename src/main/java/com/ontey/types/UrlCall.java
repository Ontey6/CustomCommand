package com.ontey.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ontey.CustomCommand;
import com.ontey.execution.Execution;
import com.ontey.files.Config;
import com.ontey.holder.Paths;
import com.ontey.holder.Placeholder;
import com.ontey.log.Log;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import static com.ontey.execution.Formattation.formattedMessage;

public class UrlCall {
   public String url, userAgent, jsonPath, name;
   
   public CustomCommand after;
   
   public UrlCall(String commandName, YamlConfiguration config, File file, String name, String url, String userAgent, String jsonPath) {
      this.name = name;
      this.url = url;
      this.userAgent = userAgent;
      this.jsonPath = jsonPath;
      this.after = new CustomCommand(config, file, Paths.UrlCall.after(commandName, name));
   }
   
   public void execute(Execution exe) {
      try {
         HttpURLConnection conn = (HttpURLConnection) URI.create(formattedMessage(url, exe)).toURL().openConnection();
         conn.setRequestProperty("User-Agent", formattedMessage(userAgent, exe));
         
         InputStreamReader reader = new InputStreamReader(conn.getInputStream());
         JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
         String str = jsonPath == null ? obj.toString() : obj.get(formattedMessage(jsonPath, exe)).getAsString();
         
         executeCommand(exe, str);
      }
      catch(MalformedURLException e) {
         Log.info("Malformed URL: " + this.url, "Not running command!");
      }
      catch(IOException ignored) {
         Log.info("Couldn't open / read from the connection. Not running command!");
      }
   }
   
   private void executeCommand(Execution exe, String result) {
      after.storage.add(Config.singletonList(new Placeholder("result", result)));
      after.execute(exe.sender, exe.label, exe.args);
   }
}
