package com.example.batool.translator;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Yandex base URL translate request
    final static String YANDEX_BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";

    // Yandex URL translate request key parameter
    final static String PARAM_KEY = "key";

    // Yandex URL translate request text parameter
    final static String PARAM_TEXT = "text";

    // Yandex URL translate request language pair parameter
    final static String PARAM_LANG_PAIR = "lang";

    // Enter your Yandex API key here
    final static String YANDEX_KEY = "";

    // A string to obtain the text to be translated from mInputEditText
    String mTextToBeTranslated;

    // Initialize the language pair to translate from English to French
    String mLanguagePair = "en-fr";


    // TextView to display the input language
    TextView mInputHeader;

    // TextView to display the output language
    TextView mOutputHeader;

    // EditText to obtain the text to be translated
    EditText mInputEditText;

    // TextView to display the translation result
    TextView mOutputTextView;

    // A view that is displayed when the there is no connection
    LinearLayout mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the input and output text views
        mInputEditText = findViewById(R.id.input_text);
        mOutputTextView = findViewById(R.id.output_text);

        // Find the input text header
        mInputHeader = findViewById(R.id.input_header);

        // Find the empty view
        mEmptyView = findViewById(R.id.empty_view);


        // Set a key listener on the EditText view and translate when the user clicks Enter
        mInputEditText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {

                if (event.getAction() != KeyEvent.ACTION_DOWN) {

                    return false;

                } else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    // Hide the empty view if it's been already displayed to the user
                    mEmptyView.setVisibility(View.GONE);

                    // Hide the translation output views
                    displayTranslationOutput(false);

                    // If there is a network connection, fetch data
                    if (NetworkUtils.isConnected(MainActivity.this)) {

                        // When the user clicks enter, hide the keyboard
                        InputMethodManager inputMethodManager =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(mInputEditText.getWindowToken(), 0);

                        // Get user input and replace white spaces with %20
                        mTextToBeTranslated = mInputEditText.getText().toString().replaceAll(" ", "%20");

                        translate(mTextToBeTranslated, mLanguagePair);

                        displayTranslationOutput(true);

                    } else {

                        // If there was no network connection, display the empty view contents
                        mEmptyView.setVisibility(View.VISIBLE);

                    }

                    return true;
                }

                return false;
            }
        });


        // Find the swap button
        ImageView swapLangsButton = findViewById(R.id.swap_langs_button);

        // Set a click listener on the swap button
        swapLangsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Swap translation results
                swapLanguages();

            }
        });
    }

    // Prepare a new URL request and execute the task
    public void translate(String textToBeTranslated, String languagePair) {

        // Build the URL translation query with the given values
        Uri builtUri = Uri.parse(YANDEX_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_KEY, YANDEX_KEY)
                .appendQueryParameter(PARAM_TEXT, textToBeTranslated)
                .appendQueryParameter(PARAM_LANG_PAIR, languagePair).build();

        // Execute the request on a background thread
        TranslateAsyncTask task = new TranslateAsyncTask();
        task.execute(builtUri.toString());
    }


    // Swap languages and translate
    public void swapLanguages() {

        // If there is a network connection, fetch data
        if (NetworkUtils.isConnected(MainActivity.this)) {

            mOutputHeader = findViewById(R.id.output_header);

            TextView inputLanguageTextView = findViewById(R.id.input_language);

            TextView outputLanguageTextView = findViewById(R.id.output_language);

            if (mLanguagePair.equals(getString(R.string.en_fr))) {

                mLanguagePair = getString(R.string.fr_en);

                // Swap languages
                inputLanguageTextView.setText(getString(R.string.french));
                outputLanguageTextView.setText(getString(R.string.english));

                // Swap headers
                mInputHeader.setText(getString(R.string.french));
                mOutputHeader.setText(getString(R.string.english));

            } else {

                mLanguagePair = getString(R.string.en_fr);

                // Swap languages
                inputLanguageTextView.setText(getString(R.string.english));
                outputLanguageTextView.setText(getString(R.string.french));

                // Swap headers
                mInputHeader.setText(getString(R.string.english));
                mOutputHeader.setText(getString(R.string.french));
            }

            String output = mOutputTextView.getText().toString();
            mInputEditText.setText(output);

            translate(output, mLanguagePair);

        } else {

            //If there was no network connection
            // Reset the input text field
            mInputEditText.setText("");

            // Display the empty view contents
            mEmptyView.setVisibility(View.VISIBLE);

            displayTranslationOutput(false);
        }
    }


    // Display the translation output views if there was a result otherwise hide them
    private void displayTranslationOutput(boolean displayStatus) {

        // LinearLayout the contains the translation output Views (mOutputHeader and mOutputTextView)
        LinearLayout translationOutputView = findViewById(R.id.translation_output_box);

        // TextView to display Yandex copyrights
        TextView copyrightTextView = findViewById(R.id.yandex_copyright);


        // If displayStatus was false
        if (!displayStatus) {

            // Hide the input text header
            mInputHeader.setVisibility(View.GONE);

            // Hide the translation output box
            translationOutputView.setVisibility(View.GONE);

            // Hide the copyright view
            copyrightTextView.setVisibility(View.GONE);

        } else {

            // Show the input text header
            mInputHeader.setVisibility(View.VISIBLE);

            // Show translation output box
            translationOutputView.setVisibility(View.VISIBLE);

            // Display Yandex copyright
            copyrightTextView.setVisibility(View.VISIBLE);

            // Set a click listener on the TextView which sends an intent to a web browser to open Yandex website
            copyrightTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Uri webPage = Uri.parse("http://translate.yandex.com/");

                    Intent intent = new Intent(Intent.ACTION_VIEW, webPage);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }

    }

    // Implementation of AsyncTask to fetch translation from Yandex translate API on a background thread
    private class TranslateAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // If the URL is null, don't perform the request
            if (url[0] == null) {
                return null;
            }

            /* Perform HTTP request for the text translation
             * then process the response and return the result */
            return NetworkUtils.fetchTranslation(url[0]);
        }

        @Override
        protected void onPostExecute(String translation) {

            if(translation != null && !translation.equals("")){
                // Update the output TextView with the resulting translation
                mOutputTextView.setText(translation);
            }
        }
    }
}
