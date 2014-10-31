package eu.corre.minder;

import java.util.ArrayList;

import eu.corre.minder.R;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class ViewIdeasList extends Activity implements
		OnItemClickListener, EnablesServerRequest {

	public Integer[] ideaIds;
	public String[] titles;

	public RelativeLayout messageLayout;
	public TextView message;
	public RelativeLayout contentLayout;
	public RelativeLayout lightbulbLayout;
	public ImageView lightbulb;
	public AnimationDrawable lightbulbAnimation;
	public ListView ideasList;
	public String context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_ideas_list);

		Bundle extras = getIntent().getExtras();
		context = extras.getString("context");

		messageLayout = (RelativeLayout) findViewById(R.id.list_view_message_layout);
		message = (TextView) findViewById(R.id.list_view_message);
		contentLayout = (RelativeLayout) findViewById(R.id.content_view_layout);
		lightbulbLayout = (RelativeLayout) findViewById(R.id.list_view_lightbulb_layout);
		lightbulb = (ImageView) findViewById(R.id.list_view_lightbulb);
		lightbulb.setBackgroundResource(R.drawable.lightbulb_animation);
		lightbulbAnimation = (AnimationDrawable) lightbulb.getBackground();
		lightbulbAnimation.start();

		ideasList = (ListView) findViewById(R.id.ideas_list_view);
		ideasList.setOnItemClickListener(this);
	}

	private void getIdeas() {
		if (context.equals("myIdeas")) {
			new ServerRequestTask(this).execute(
					"select id, title from ideas where user_id=?;",
					GeneralSettings.userID, "get ideas");
		} else {
			new ServerRequestTask(this)
					.execute(
							"select ideas.id, ideas.title, users.name from users, "
									+ "ideas inner join rated_ideas on ideas.id=rated_ideas.idea_id "
									+ "where rated_ideas.user_id=? and rated_ideas.rating=1 and ideas.user_id=users.id;",
							GeneralSettings.userID, "get ideas");
		}
	}

	@Override
	public void onResume() {
		getIdeas();
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void handleRequestResult(ArrayList<Object[]> result,
			String requestContext) {
		int n = result.size();
		if (n == 0) {
			lightbulbAnimation.stop();
			lightbulbLayout.setVisibility(View.GONE);
			messageLayout.setVisibility(View.VISIBLE);
			if (context.equals("myIdeas")) {
				message.setText(R.string.no_created_ideas);
			} else {
				message.setText(R.string.no_liked_ideas);
			}
		} else {
			ideaIds = new Integer[n];
			titles = new String[n];
			for (int i = 0; i < n; i++) {
				ideaIds[i] = (Integer) result.get(i)[0];
				titles[i] = (String) result.get(i)[1];
			}
			if (context.equals("myIdeas")) {
				CustomArrayAdapter adapter = new CustomArrayAdapter(this,
						titles, null);
				ideasList.setAdapter(adapter);

				lightbulbAnimation.stop();
				lightbulbLayout.setVisibility(View.GONE);
				contentLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}
}
