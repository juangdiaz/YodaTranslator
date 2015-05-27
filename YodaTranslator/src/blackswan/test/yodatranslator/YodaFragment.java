package blackswan.test.yodatranslator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class YodaFragment extends Fragment {

	EditText englishText;
	TextView yodishText;
	Button convertButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_yodatran, container,
				false);

		// Get a reference to the TextView used to display Yodish
		yodishText = (TextView) view.findViewById(R.id.yodish_text);

		englishText = (EditText) view.findViewById(R.id.english_text);

		convertButton = (Button) view.findViewById(R.id.convert_button);
		convertButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				YodaLoader convertTask = new YodaLoader();
				convertTask.execute(englishText.getText().toString());
				convertButton.setEnabled(false);
				convertButton.setText(R.string.convert_waiting);

			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	public class YodaLoader extends AsyncTask<String, Void, String> {
		private final String LOG_TAG = "YodaLoader";

		// Called before doInBackground(). Runs on the UI thread.
		@Override
		protected void onPreExecute() {
		}

		// Runs on a background thread.
		@Override
		protected String doInBackground(String... params) {

			if (params.length < 1) {
				// nothing to translate
				return null;
			}

			String strEnglish = params[0];
			String strYodish = null;

			HttpURLConnection httpUrlConnection = null;
			BufferedReader result = null;

			try {
				// Build the URL for the yodish query.
				Uri builtUri = Uri.parse("https://yoda.p.mashape.com/yoda")
						.buildUpon()
						.appendQueryParameter("sentence", strEnglish).build();
				URL url = new URL(builtUri.toString());

				// Make the request to YodaSpeak.
				httpUrlConnection = (HttpURLConnection) url.openConnection();
				httpUrlConnection.setRequestProperty("X-Mashape-Key",
						"nrI0CIHuZQmsh47tVA8ML1Ljmz0Op19EVXGjsn4C7wsw62hJlE");
				httpUrlConnection.setRequestMethod("GET");
				httpUrlConnection.connect();

				// Get result
				InputStream inputStream = httpUrlConnection.getInputStream();
				StringBuffer buffer = new StringBuffer();

				if (inputStream == null) {
					return null;
				}

				Reader inputStreamReader = new InputStreamReader(inputStream);
				result = new BufferedReader(inputStreamReader);

				String line;
				while ((line = result.readLine()) != null) {
					buffer.append(line + "\n");
				}

				if (buffer.length() > 0) {
					strYodish = buffer.toString();
				}

			} catch (IOException e) {
				Log.e(LOG_TAG, "IOException reading stream", e);
				return null;
			} finally {
				if (httpUrlConnection != null) {
					httpUrlConnection.disconnect();
				}
				if (result != null) {
					try {
						result.close();
					} catch (final IOException e) {
						Log.e(LOG_TAG, "Error closing stream", e);
					}
				}
			}

			return strYodish;
		}

		// Called after and with the result of doInBackground(). Runs on the UI
		// thread.
		@Override
		protected void onPostExecute(String result) {

			convertButton.setEnabled(true);

			if (result != null) {
				yodishText.setText(result);
				convertButton.setText(R.string.convert_button);
				englishText.setText("");
			}
		}

	}

}