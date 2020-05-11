package app.personal;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebHelper extends AsyncTask<Void, Void, Void> {
    public static File file;
    InputStream is;

    protected void doInBackground() throws IOException {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        file = new File(path, "DemoPicture.jpg");

        try {
            // Make sure the Pictures directory exists.
            path.mkdirs();

            URL url = new URL("http://androidsaveitem.appspot.com/downloadjpg");
            /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

            /*
             * Define InputStreams to read from the URLConnection.
             */
            is = ucon.getInputStream();

            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();

        } catch (IOException e) {
            Log.d("ImageManager", "Error: " + e);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // TODO Auto-generated method stub
        try {
            doInBackground();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute() {
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(null,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }
}
