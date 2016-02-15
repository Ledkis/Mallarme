package ledkis.module.mallarme;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ledkis on 15/02/2016.
 */
public class Utils {

    public static String readTextFileFromAssets(Context context,
                                                String fileName) {
        StringBuilder body = new StringBuilder();

        try {
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStream);
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open assets: " + fileName, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Assets not found: "
                    + fileName, nfe);
        }

        return body.toString();
    }

}
