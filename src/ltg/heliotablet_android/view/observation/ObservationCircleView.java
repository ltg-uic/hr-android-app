package ltg.heliotablet_android.view.observation;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import ltg.heliotablet_android.R;
import ltg.heliotablet_android.R.color;
import ltg.heliotablet_android.data.Reason;
import ltg.heliotablet_android.view.ICircleView;
import ltg.heliotablet_android.view.PopoverView;
import ltg.heliotablet_android.view.PopoverViewAdapter;
import ltg.heliotablet_android.view.PopoverView.PopoverViewDelegate;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;

public class ObservationCircleView extends RelativeLayout implements ICircleView, PopoverViewDelegate {

	private String flag;
	private int textColor;
	private TextView reasonTextView;
	private ImmutableSortedSet<Reason> imReasons;
	private String anchor;
	private GestureDetector gestureDetector;

	private PopoverView cachedPopoverView;
	private RelativeLayout viewPagerLayout;
	private Reason reasonNeedsUpdate;
	private Multiset<String> reasonTextMultiSet;
	
	public ObservationCircleView(Context context) {
		super(context);
	}
	
	public ObservationCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.circle_view, this, true);
        reasonTextView = (TextView) getChildAt(0);
        
        TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.CircleView);
        this.flag = a.getString(R.styleable.CircleView_flag);
        this.setTextColor(a.getColor(R.styleable.CircleView_textColor, color.White));
        a.recycle();
        
        viewPagerLayout = (RelativeLayout) inflater.inflate(
				R.layout.activity_screen_slide, this, false);
        reasonTextView.setTextColor(this.getTextColor());
        this.enableDoubleTap();
    }
	
	
	public void showPopover(ImmutableSortedSet<Reason> popOverReasonSet) {
		Resources resources = getResources();
		ArrayList<View> pages = new ArrayList<View>();
		ViewPager pager = new ViewPager(getContext());
		ViewPager vPager = (ViewPager) viewPagerLayout
				.findViewById(R.id.pager);
		
		Reason reason = popOverReasonSet.first();
			View layout = View.inflate(getContext(),
					R.layout.popover_view_obs_choose_delete, null);
			pages.add(layout);
			
			
			TextView reasonText = (TextView) layout.findViewById(R.id.reasonTitle);
			String text = String.format(resources.getString(R.string.obs_reason_text), reason.getFlag(), reason.getAnchor());
			
			reasonText.setText(text);
			
			
			
			RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radioGroup);
			radioGroup.setTag(reason);
			applyCountToLabelRadioButtons(radioGroup, flag, anchor);
			
			radioGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup rg,
								int checkedId) {
							
							Reason reason = (Reason) rg.getTag();
							
							String flag = StringUtils.capitalize(reason.getFlag());
							String anchor = StringUtils.capitalize(reason.getAnchor());

							ObservationCircleView.this
									.applyCountToLabelRadioButtons(rg, flag, anchor);
							RadioButton selectedRadioButton = (RadioButton) rg.findViewById(checkedId);

							String text = (String) selectedRadioButton.getTag();
							selectedRadioButton.setText(createRadioButtonCountLabel(text,1));

						}
					});
			
			Button deleteButton = (Button) layout.findViewById(R.id.deleteButton);
			
				deleteButton.setVisibility(View.VISIBLE);
				deleteButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						
						
					}
				});
		
		vPager.setAdapter(new PopoverViewAdapter(pages));
		vPager.setOffscreenPageLimit(5);
		vPager.setCurrentItem(0);
        ViewGroup parent = (ViewGroup) this.getParent().getParent().getParent();

		
		PopoverView popoverView = new PopoverView(getContext(), viewPagerLayout);
		cachedPopoverView = popoverView;
		popoverView.setContentSizeForViewInPopover(new Point(520, 220));
		popoverView.setDelegate(this);
		popoverView.showPopoverFromRectInViewGroup(
				parent,
				PopoverView.getFrameForView(this),
				PopoverView.PopoverArrowDirectionAny, true);
		
	}
	
	private void applyCountToLabelRadioButtons(RadioGroup radioGroup, String flag, String anchor) {
		int childCount = radioGroup.getChildCount();
		Resources resources = getResources();
		
		for( int i = 0; i < childCount; i++) {
			RadioButton r = (RadioButton)radioGroup.getChildAt(i);
			
			try {
			    Class res = R.string.class;
			    Field field = res.getField("stringName");
			    int stringId = field.getInt(r.getId());
			    String text = String.format(resources.getString(stringId), flag, anchor);
			    r.setTag(text);
				r.setText(createRadioButtonCountLabel(text,0));
			}
			catch (Exception e) {
			    Log.e("MyTag", "Failure to get drawable id.", e);
			}
		}
		
	}

	private String createRadioButtonCountLabel(String text, int extraCount) {
		int count = reasonTextMultiSet.count(text);
		return text + ((count > 0) ? "(" + count + extraCount + ")" : "" ); 
		
	}

	@Override
	public void popoverViewWillShow(PopoverView view) {
	}

	@Override
	public void popoverViewDidShow(PopoverView view) {
	
	}

	public void popoverViewWillDismiss(PopoverView view) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void popoverViewDidDismiss(PopoverView view) {
		// TODO Auto-generated method stub
		
	}


	public int getReasonsSize() {
		if( imReasons != null ){
			imReasons.size();
		}
		return 0;
	}
	
	@Override
	public void setTag(Object tag) {
		super.setTag(tag);
		
		imReasons = (ImmutableSortedSet<Reason>) tag;
		
		if( imReasons != null) {
			this.reasonTextView.setText(""+ imReasons.size());
			//also make a multiset
		}
		
	}

	public void makeToast(String toastText) {
		Toast toast = Toast.makeText(getContext(), toastText,
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
		toast.show();
	}
	
	
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	public void setReasonText(String reasonText) {
		this.reasonTextView.setText(reasonText);
	}
	
	public String getReasonText() {
		return this.reasonTextView.getText().toString();
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	    reasonTextView.setTextColor(textColor);
	}

	public void setReducedAlpha(float circleViewAlpha) {
		Drawable background = getBackground();
		background.setAlpha((int) circleViewAlpha);
		setBackground(background);
		reasonTextView.setAlpha(circleViewAlpha);
	}
	
	protected void makeTransparent(boolean isTransparent) {
		if( isTransparent )
			this.setReducedAlpha(100f);
		else
			this.setReducedAlpha(255f);
		
	}
	
	@Override
	public String toString() {
		return "Flag: " + this.getFlag() + " Reasons: " + imReasons.toString();
	}
	
	public String getAnchor() {
		return anchor;
	}

	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	
	public void enableDoubleTap() {
        gestureDetector = new GestureDetector(getContext(), new CircleDoubleTapGestureListener());
        this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 gestureDetector.onTouchEvent(event);
				 return true;
			}
		}); 
	}
	
	public Multiset<String> getReasonTextMultiSet() {
		return reasonTextMultiSet;
	}

	public void setReasonTextMultiSet(Multiset<String> reasonTextMultiSet) {
		this.reasonTextMultiSet = reasonTextMultiSet;
	}

	class CircleDoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {
	    // event when double tap occurs
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {
	    	
	    	showPopover(imReasons);
	        return super.onDoubleTap(e);
	    }
	}


	

	


}