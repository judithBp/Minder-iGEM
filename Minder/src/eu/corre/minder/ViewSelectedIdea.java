package eu.corre.minder;

import java.util.ArrayList;

import eu.corre.minder.dialogs.AlertDecisionDialogFragment;
import eu.corre.minder.R;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewSelectedIdea extends FragmentActivity implements
		OnClickListener, EnablesServerRequest, EnablesDialog {

	private static final String TAG = "ViewMyIdeaFragmentActivity";

	private Integer ideaId;
	private String context;
	private String idea;
	private String creator;
	private String info;
	private String contact;
	private int language;
	private int likes;
	private int dislikes;

	private RelativeLayout ratingLayout;
	private RelativeLayout contactLayout;
	private RelativeLayout btnLayoutMyIdea;
	private RelativeLayout btnLayoutNewLike;
	private RelativeLayout btnLayoutLikedIdea;
	private RelativeLayout contentLayout;
	private RelativeLayout lightbulbLayout;
	private AnimationDrawable lightbulbAnimation;
	private ImageView lightbulb;

	private TextView ideaView;
	private TextView creatorView;
	private TextView infoView;
	private TextView contactView;
	private TextView likesView;
	private TextView dislikesView;
	private Button btnEdit;
	private Button btnDelete;
	private Button btnNext;
	private Button btnDeleteLike;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_selected_idea);

		Bundle extras = getIntent().getExtras();
		ideaId = extras.getInt("ideaId");
		context = extras.getString("context");
		idea = extras.getString("idea");
		info = extras.getString("info");
		contact = extras.getString("contact");

		ideaView = (TextView) findViewById(R.id.view_selected_idea);
		creatorView = (TextView) findViewById(R.id.view_selected_creator);
		infoView = (TextView) findViewById(R.id.view_selected_info);

		ideaView.setText(idea);
		infoView.setText(info);

		contentLayout = (RelativeLayout) findViewById(R.id.content_view_selected);
		lightbulbLayout = (RelativeLayout) findViewById(R.id.view_selected_lightbulb_layout);
		lightbulb = (ImageView) findViewById(R.id.view_selected_lightbulb);
		lightbulb.setBackgroundResource(R.drawable.lightbulb_animation);
		lightbulbAnimation = (AnimationDrawable) lightbulb.getBackground();

		if (context.equals("myIdea")) {
			language = extras.getInt("language");
			likes = extras.getInt("likes");
			dislikes = extras.getInt("dislikes");

			creatorView.setText("by me");

			likesView = (TextView) findViewById(R.id.view_selected_likes);
			dislikesView = (TextView) findViewById(R.id.view_selected_dislikes);

			likesView.setText(Integer.toString(likes));
			dislikesView.setText(Integer.toString(dislikes));

			btnEdit = (Button) findViewById(R.id.view_selected_btn_edit);
			btnDelete = (Button) findViewById(R.id.view_selected_btn_delete);

			btnEdit.setOnClickListener(this);
			btnDelete.setOnClickListener(this);
		} else {
			ratingLayout = (RelativeLayout) findViewById(R.id.view_selected_rating_layout);
			contactLayout = (RelativeLayout) findViewById(R.id.view_selected_contact_layout);
			btnLayoutMyIdea = (RelativeLayout) findViewById(R.id.view_selected_button_layout_my_idea);

			contactView = (TextView) findViewById(R.id.view_selected_contact);
			contactView.setText("Contact: " + contact);

			creator = extras.getString("creator");
			creatorView.setText("by " + creator);

			btnLayoutMyIdea.setVisibility(View.GONE);
			ratingLayout.setVisibility(View.GONE);
			contactLayout.setVisibility(View.VISIBLE);

			if (context.equals("newLiked")) {
				btnLayoutNewLike = (RelativeLayout) findViewById(R.id.view_selected_button_layout_new_liked);

				btnLayoutNewLike.setVisibility(View.VISIBLE);

				btnNext = (Button) findViewById(R.id.view_selected_btn_next);

				btnNext.setOnClickListener(this);
			} else if (context.equals("likedIdea")) {
				btnLayoutLikedIdea = (RelativeLayout) findViewById(R.id.view_selected_button_layout_liked_idea);

				btnLayoutLikedIdea.setVisibility(View.VISIBLE);

				btnDeleteLike = (Button) findViewById(R.id.view_selected_btn_delete_liked);

				btnDeleteLike.setOnClickListener(this);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_selected_btn_edit:
			contentLayout.setVisibility(View.GONE);
			lightbulbLayout.setVisibility(View.VISIBLE);
			lightbulbAnimation.start();
			new ServerRequestTask(this).execute(
					"select tag from tags_ideas where id=?;",
					Integer.toString(ideaId), "get tags");
			break;
		case R.id.view_selected_btn_delete:
			showDialog();
			break;
		case R.id.view_selected_btn_next:
			super.onBackPressed();
			break;
		case R.id.view_selected_btn_delete_liked:
			showDialog();
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void handleRequestResult(ArrayList<Object[]> result,
			String requestContext) {
		int n = result.size();

		if (requestContext.equals("get tags")) {
			String[] tags = new String[n];
			for (int i = 0; i < n; i++) {
				tags[i] = (String) result.get(i)[0];
			}

			Intent mIntent = new Intent(this, CreateIdea.class);
			mIntent.putExtra("ideaId", ideaId);
			mIntent.putExtra("idea", idea);
			mIntent.putExtra("tags", tags);
			mIntent.putExtra("info", info);
			mIntent.putExtra("language", language);
			mIntent.putExtra("contact", contact);
			mIntent.putExtra("context", "edit");
			startActivity(mIntent);
			finish();
		} else if (requestContext.equals("get rating")) {
			int rating = (int) result.get(0)[0];
			switch (rating) {
			case -1:
				new ServerRequestTask(this)
						.execute("update ideas set reports=reports-1 where id="
								+ ideaId,
								"delete from rated_ideas where idea_id="
										+ ideaId + " and user_id='"
										+ GeneralSettings.userID + "'",
								"update table");

				break;
			case 0:
				new ServerRequestTask(this).execute(
						"update ideas set dislikes=dislikes-1 where id="
								+ ideaId,
						"delete from rated_ideas where idea_id=" + ideaId
								+ " and user_id='" + GeneralSettings.userID
								+ "'", "update table");

				break;
			case 1:
				new ServerRequestTask(this).execute(
						"update ideas set likes=likes-1 where id=" + ideaId,
						"delete from rated_ideas where idea_id=" + ideaId
								+ " and user_id='" + GeneralSettings.userID
								+ "'", "update table");

				break;
			}
			super.onBackPressed();
		}
	}

	private void showDialog() {
		if (context.equals("myIdea")) {
			DialogFragment newFragment = AlertDecisionDialogFragment
					.newInstance("Are you sure you want to delete the idea?");
			newFragment.show(getSupportFragmentManager(), TAG);
		} else {
			DialogFragment newFragment = AlertDecisionDialogFragment
					.newInstance("Are you sure you want to delete your rating on this idea?");
			newFragment.show(getSupportFragmentManager(), TAG);
		}
	}

	@Override
	public void positiveSelected() {
		contentLayout.setVisibility(View.GONE);
		lightbulbLayout.setVisibility(View.VISIBLE);
		lightbulbAnimation.start();

		if (context.equals("myIdea")) {

			new ServerRequestTask(this)
					.execute("delete from ideas where id=" + ideaId,
							"delete from rated_ideas where idea_id=" + ideaId,
							"delete from tags_ideas where id=" + ideaId,
							"update table");
			super.onBackPressed();
		} else {
			new ServerRequestTask(this)
					.execute(
							"select rating from rated_ideas where idea_id=? and user_id=?;",
							Integer.toString(ideaId), GeneralSettings.userID,
							"get rating");
			GeneralSettings.refresh = true;
		}
	}

	@Override
	public void negativeSelected() {
	}

}
