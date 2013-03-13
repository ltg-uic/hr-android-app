package ltg.heliotablet_android.view.observation;

import java.util.Collections;
import java.util.List;

import ltg.heliotablet_android.MiscUtil;
import ltg.heliotablet_android.R;
import ltg.heliotablet_android.data.Reason;
import ltg.heliotablet_android.data.ReasonDBOpenHelper;
import ltg.heliotablet_android.view.controller.ObservationReasonController;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.google.common.collect.Lists;

public class ObservationViewFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private String observationAnchor;
	private ObservationReasonController observationController;
	private ReasonDBOpenHelper db = null;
	private ObservationAnchorView observationAnchorView;
	private SQLiteCursorLoader loader = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// init theory

		observationAnchorView = (ObservationAnchorView) inflater.inflate(
				R.layout.observation_anchor_view, container, false);
		observationAnchorView.setAnchor(observationAnchor);
		observationAnchorView.setOnDragListener(new TargetViewDragListener());

		db = ReasonDBOpenHelper.getInstance(this.getActivity());

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());

		observationController = ObservationReasonController
				.getInstance(getActivity());
		observationController.add(observationAnchor, observationAnchorView);
		// setupTestListeners();
		setupLoader();

		return observationAnchorView;
	}

	private void setupLoader() {
		getLoaderManager().initLoader(
				ReasonDBOpenHelper.ALL_REASONS_OBS_LOADER_ID, null, this);
		LoaderManager.enableDebugLogging(true);
	}

	class TargetViewDragListener implements OnDragListener {

		@Override
		public boolean onDrag(View targetView, DragEvent event) {

			// Dropped, reassign View to ViewGroup
			View dragged = (View) event.getLocalState();
			ViewGroup sourceView = (ViewGroup) dragged.getParent();

			int action = event.getAction();
			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:

				if (targetView.equals(sourceView)) {
					Log.i("DRAG", "there equal");
				} else {
					// sourceView.removeView(dragged);

					// RelativeLayout rel = (RelativeLayout) dragged;

					if (targetView instanceof ObservationAnchorView) {
						ObservationAnchorView tv = (ObservationAnchorView) targetView;
						ObservationCircleView cv = (ObservationCircleView) dragged;

						if (!tv.getAnchor().equals(cv.getFlag())) {
							
							if( tv.hasReadyOnlyFalse(cv.getFlag()) == true ) {
									MiscUtil.makeTopToast(ObservationViewFragment.this.getActivity(), "Only one allowed at time.");
									return false;
							}
							
							
							Reason reason = new Reason(tv.getAnchor(),
									cv.getFlag(), Reason.TYPE_OBSERVATION,
									"tony", false);
							observationController.insertReason(reason);
						} else {
							MiscUtil.makeTopToast((Context)getActivity(),  "Can't drop the same colors.");
						}
					}
				}
				break;
			case DragEvent.ACTION_DRAG_ENDED:
			case DragEvent.ACTION_DRAG_LOCATION:
				View v = (View) event.getLocalState();
				v.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			return true;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
		case ReasonDBOpenHelper.ALL_REASONS_OBS_LOADER_ID:
			return new SQLiteCursorLoader(
					this.getActivity(),
					db,
					"SELECT _id, anchor, type, flag, reasonText, isReadOnly, lastTimestamp, origin FROM reason WHERE type = '"
							+ ReasonDBOpenHelper.TYPE_OBSERVATION
							+ "' AND anchor = '"
							+ observationAnchor
							+ "' ORDER BY anchor, flag, isReadOnly;", null);
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		switch (loader.getId()) {
		case ReasonDBOpenHelper.ALL_REASONS_OBS_LOADER_ID:
			updateAllViews(data);
			break;
		}
		Log.d("OBSERVATION FRAGMENT", "onLoadFinished");
	}

	private void updateAllViews(Cursor data) {
		if (data != null && data.getCount() > 0) {
			List<Reason> allReasons = Lists.newArrayList();
			allReasons = Collections.synchronizedList(allReasons);
			data.moveToFirst();
			while (!data.isAfterLast()) {
				Reason reason = ReasonDBOpenHelper.cursorToReason(data);
				allReasons.add(reason);
				data.moveToNext();
			}
			quickDump(allReasons);
			observationController.updateViews(allReasons,
					this.observationAnchor);
		}
	}

	private void quickDump(List<Reason> allReasons) {
		for (Reason reason : allReasons) {
			System.out.println("REASON: " + reason.toString());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		db.close();
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs,
			Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);

		TypedArray a = activity.obtainStyledAttributes(attrs,
				R.styleable.ObservationViewFragment);

		observationAnchor = StringUtils.lowerCase((String) a
				.getText(R.styleable.ObservationViewFragment_android_label));
		a.recycle();

	}

}