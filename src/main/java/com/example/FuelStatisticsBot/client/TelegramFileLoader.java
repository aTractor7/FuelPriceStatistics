package com.example.FuelStatisticsBot.client;

import com.example.FuelStatisticsBot.util.exception.ClientException;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;

@Component
public class TelegramFileLoader {

    @Value("${bot.token}")
    private String botToken;
    @Value("${telegram.file.url.inf}")
    private String fileInfUrl;
    @Value("${telegram.file.url.download}")
    private String fileLoadUrl;
    @Value("${fuel.dates.file.pass}")
    private String localFilePath;

    public File loadFile(String fileId) {
        try {
            String uploadedFilePath = getFilePath(fileId);

            File file = new File(localFilePath);
            InputStream downloadStream = new URL(String.format(fileLoadUrl, botToken, uploadedFilePath)).openStream();

            FileUtils.copyInputStreamToFile(downloadStream, file);

            downloadStream.close();

            return file;
        } catch (IOException e) {
            throw new ClientException("Exception due to connecting to telegram download url.", e);
        }
    }

    private String getFilePath(String fileId) throws IOException {
        URL url = new URL(String.format(fileInfUrl, botToken, fileId));

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = bufferedReader.readLine();

        JSONObject result = new JSONObject(res);
        JSONObject path = result.getJSONObject("result");

        bufferedReader.close();
        return path.getString("file_path");
    }
}
