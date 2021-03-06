package eu.corre.minder;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class ViewMyIdeasList extends ViewIdeasList {

	private String info;
	private String contact;
	private int language;
	private int likes;
	private int dislikes;
	private int pos;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void handleRequestResult(ArrayList<Object[]> result,
			String requestContext) {
		if (requestContext.equals("get ideas")) {
			super.handleRequestResult(result, requestContext);
		} else {
			info = (String) result.get(0)[0];
			language = (int) result.get(0)[1];
			contact = (String) result.get(0)[2];
			likes = (int) result.get(0)[3];
			dislikes = (int) result.get(0)[4];

			Intent mIntent = new Intent(this, ViewSelectedIdea.class);
			mIntent.putExtra("context", "myIdea");
			mIntent.putExtra("idea", super.titles[pos]);
			mIntent.putExtra("ideaId", super.ideaIds[pos]);
			mIntent.putExtra("info", info);
			mIntent.putExtra("contact", contact);
			mIntent.putExtra("language", language);
			mIntent.putExtra("likes", likes);
			mIntent.putExtra("dislikes", dislikes);
			startActivity(mIntent);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		super.contentLayout.setVisibility(View.GONE);
		super.lightbulbLayout.setVisibility(View.VISIBLE);
		super.lightbulbAnimation.start();
		pos = position;
		new ServerRequestTask(this)
				.execute(
						"select info, language, contact, likes, dislikes from ideas where id=?;",
						Integer.toString(super.ideaIds[pos]),
						"get selected idea");
	}

}
