package eu.corre.minder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import eu.corre.minder.dialogs.AlertDecisionDialogFragment;
import eu.corre.minder.R;

public class Homepage extends FragmentActivity implements
		OnMenuItemClickListener, EnablesDialog {

	private static final String TAG = "HomepageActivity";

	private Intent mIntent;
	private HomepageFragmentLayout fragment = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage_layout);

		try {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			fragment = new HomepageFragmentLayout();
			ft.add(R.id.homepage_main_content_fragment, fragment);
			ft.commit();
		} catch (Exception e) {
			Log.e(TAG, "Exception on creating fragment: " + e);
		}
	}

	public void showPopup(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(this);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.homepage_menu, popup.getMenu());
		popup.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		fragment.setNewIdeaParameter(false);
		switch (item.getItemId()) {
		case R.id.create_idea:
			mIntent = new Intent(this, CreateIdea.class);
			mIntent.putExtra("context", "create");
			startActivity(mIntent);
			return true;
		case R.id.view_ideas:
			mIntent = new Intent(this, ViewMyIdeasList.class);
			mIntent.putExtra("context", "myIdeas");
			startActivity(mIntent);
			return true;
		case R.id.liked_ideas:
			mIntent = new Intent(this, ViewLikedIdeasList.class);
			mIntent.putExtra("context", "likedIdeas");
			startActivity(mIntent);
			return true;
		case R.id.settings:
			fragment.setNewIdeaParameter(true);
			mIntent = new Intent(this, Settings.class);
			startActivity(mIntent);
			return true;
		case R.id.log_out:
			mIntent = new Intent(this, Login.class);
			mIntent.putExtra("logout", true);
			startActivity(mIntent);
			finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void positiveSelected() {
		fragment.sendReport();
		fragment.getIdea();
	}

	@Override
	public void negativeSelected() {
		fragment.setBtnClickable(true);
	}

	public void showDialog() {
		DialogFragment newFragment = AlertDecisionDialogFragment
				.newInstance("Are you sure you want to report this idea?");
		newFragment.show(getSupportFragmentManager(), TAG);
	}

}
