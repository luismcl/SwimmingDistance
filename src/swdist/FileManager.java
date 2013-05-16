package swdist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

public class FileManager {

	boolean sdDisponible = false;
	boolean sdAccesoEscritura = false;

	public FileManager() {

		String estado = Environment.getExternalStorageState();

		if (estado.equals(Environment.MEDIA_MOUNTED)) {
			sdDisponible = true;
			sdAccesoEscritura = true;
		} else if (estado.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			sdDisponible = true;
			sdAccesoEscritura = false;
		} else {
			sdDisponible = false;
			sdAccesoEscritura = false;
		}
	}

	public void writeFile(String fileName, Object content) {
		Gson gson = new Gson();

		try {
			File ruta_sd = Environment.getExternalStorageDirectory();

			File f = new File(ruta_sd.getAbsolutePath(), fileName);

			OutputStreamWriter fout = new OutputStreamWriter(
					new FileOutputStream(f));

			fout.write(gson.toJson(content));
			fout.flush();
			fout.close();

		} catch (Exception ex) {
			Log.e("Ficheros", "Error al escribir fichero a tarjeta SD", ex);
		}
	}
}
