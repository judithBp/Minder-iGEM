package eu.corre.minder;

import java.util.ArrayList;

import eu.corre.minder.dialogs.AlertDecisionDialogFragment;
import eu.corre.minder.dialogs.NoTextDialogFragment;
import eu.corre.minder.R;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

public class CreateIdea extends FragmentActivity implements
		OnItemSelectedListener, EnablesServerRequest, EnablesDialog,
		TextWatcher {

	private static final String TAG = "CreateIdeaFragmentActivity";
	private boolean textChanged = false;

	private Spinner languageSpinner;
	private EditText ideaText;
	private MultiAutoCompleteTextView tagsText;
	private EditText infoText;
	private EditText contactText;
	private Button btnCreate;

	private int languagePosition;
	private String language;
	private String idea;
	private String[] tags;
	private String info;
	private String contact;
	private String context;
	private Integer ideaId;

	private ScrollView contentView;
	private RelativeLayout lightbulbLayout;
	private AnimationDrawable lightbulbAnimation;
	private ImageView lightbulb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_idea);

		languageSpinner = (Spinner) findViewById(R.id.language_idea);
		ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter
				.createFromResource(this, R.array.language_array,
						android.R.layout.simple_spinner_item);
		adapterSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageSpinner.setAdapter(adapterSpinner);
		languageSpinner.setOnItemSelectedListener(this);

		ideaText = (EditText) findViewById(R.id.new_idea);
		ideaText.addTextChangedListener(this);

		tagsText = (MultiAutoCompleteTextView) findViewById(R.id.create_tags);
		new ServerRequestTask(this).execute(
				"select tag from tags_users union select tag from tags_ideas;",
				"get all tags");
		tagsText.addTextChangedListener(this);

		infoText = (EditText) findViewById(R.id.info_create);
		infoText.addTextChangedListener(this);

		contactText = (EditText) findViewById(R.id.create_contact);
		contactText.addTextChangedListener(this);

		btnCreate = (Button) findViewById(R.id.btn_create);

		Bundle extras = getIntent().getExtras();
		context = extras.getString("context");

		contentView = (ScrollView) findViewById(R.id.create_scroll_view);
		lightbulbLayout = (RelativeLayout) findViewById(R.id.create_lightbulb_layout);
		lightbulb = (ImageView) findViewById(R.id.create_lightbulb);
		lightbulb.setBackgroundResource(R.drawable.lightbulb_animation);
		lightbulbAnimation = (AnimationDrawable) lightbulb.getBackground();
		lightbulbAnimation.start();

		if (context.equals("edit")) {

			ideaId = extras.getInt("ideaId");
			idea = extras.getString("idea");
			tags = extras.getStringArray("tags");
			info = extras.getString("info");
			contact = extras.getString("contact");
			languagePosition = extras.getInt("language");
			language = GeneralSettings.languages[languagePosition];

			ideaText.setFocusable(false);
			ideaText.setEnabled(false);

			ideaText.setText(idea);
			String tagsString = "";
			for (int i = 0; i < tags.length; i++) {
				tagsString += tags[i] + ", ";
			}
			tagsText.setText(tagsString);
			infoText.setText(info);
			contactText.setText(contact);
			languageSpinner.setSelection(languagePosition);

			btnCreate.setBackgroundResource(R.drawable.minder_ok);

			textChanged = false;
		} else {
			btnCreate.setBackgroundResource(R.drawable.minder_add);
		}
	}

	@Override
	public void onBackPressed() {
		if (textChanged && !ideaText.getText().toString().equals("")) {
			showDialog();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void positiveSelected() {
		super.onBackPressed();
	}

	@Override
	public void negativeSelected() {

	}

	public void create(View v) {
		idea = ideaText.getText().toString();

		if (idea.equals("") || idea.equals("Title")) {
			DialogFragment dialog = new NoTextDialogFragment();
			dialog.show(getSupportFragmentManager(), "NoTextDialogFragment");
		} else {
			info = infoText.getText().toString();
			if (info.equals("Additional information")) {
				info = "";
			}
			if (!tagsText.getText().toString().contains("#")) {
				tags = tagsText.getText().toString().split(",\\s");
			} else {
				tags = new String[0];
			}
			languagePosition = languageSpinner.getSelectedItemPosition();
			contact = contactText.getText().toString();
			if (contact.equals("Contact information")) {
				contact = "";
			}

			if (context.equals("edit")) {
				contentView.setVisibility(View.GONE);
				lightbulbLayout.setVisibility(View.VISIBLE);
				lightbulbAnimation.start();

				String tagString = "insert into tags_ideas(tag, id) values";
				for (int i = 0; i < tags.length; i++) {
					if (i != tags.length - 1) {
						tagString += "('" + tags[i] + "', " + ideaId + "), ";
					} else {
						tagString += "('" + tags[i] + "', " + ideaId + ")";
					}
				}

				new ServerRequestTask(this).execute("update ideas set info='"
						+ info + "', language=" + languagePosition
						+ ", contact='" + contact + "' where id=" + ideaId,
						"delete from tags_ideas where id=" + ideaId, tagString,
						"update table");
			} else {
				contentView.setVisibility(View.GONE);
				lightbulbLayout.setVisibility(View.VISIBLE);
				lightbulbAnimation.start();

				String tagString = "insert into tags_ideas(tag, id) values";
				for (int i = 0; i < tags.length; i++) {
					if (i != tags.length - 1) {
						tagString += "('" + tags[i] + "', LAST_INSERT_ID()), ";
					} else {
						tagString += "('" + tags[i] + "', LAST_INSERT_ID())";
					}
				}

				new ServerRequestTask(this)
						.execute(
								"insert into ideas(user_id, title, info, language, contact, likes, dislikes, reports) values('"
										+ GeneralSettings.userID
										+ "', '"
										+ idea
										+ "', '"
										+ info
										+ "', "
										+ languagePosition
										+ ", '"
										+ contact
										+ "', 0, 0, 0)", tagString, "set idea");
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		language = (String) parent.getItemAtPosition(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		language = "English";
	}

	@Override
	public void handleRequestResult(ArrayList<Object[]> result,
			String requestContext) {
		if (requestContext.equals("set idea")) {
			super.onBackPressed();
		} else if (requestContext.equals("update table")) {
			super.onBackPressed();
		} else if (requestContext.equals("get all tags")) {
			int n = result.size();
			String[] tagsArray = new String[n];
			for (int i = 0; i < n; i++) {
				tagsArray[i] = (String) result.get(i)[0];
			}
			tagsText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
			ArrayAdapter<String> adapterTags = new ArrayAdapter<String>(this,
					R.layout.tag_list_item, tagsArray);
			tagsText.setThreshold(1);
			tagsText.setAdapter(adapterTags);

			lightbulbAnimation.stop();
			lightbulbLayout.setVisibility(View.GONE);
			contentView.setVisibility(View.VISIBLE);
		}
	}

	private void showDialog() {
		DialogFragment newFragment = AlertDecisionDialogFragment
				.newInstance("Do you really want to leave this page? All entered information will be lost!");
		newFragment.show(getSupportFragmentManager(), TAG);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		textChanged = true;
	}
}