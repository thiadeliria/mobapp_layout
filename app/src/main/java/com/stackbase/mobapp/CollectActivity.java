package com.stackbase.mobapp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;

import com.stackbase.mobapp.activity.PreferencesActivity;
import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CollectActivity extends FragmentActivity implements ActionBar.TabListener {
    List<Fragment> frags;
    Fragment idCard, otherInfo;
    ActionBar actionBar;
    ViewPager vp;
    private SharedPreferences prefs;
    private String TAG = CollectActivity.class.getSimpleName();

    FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            return frags.get(position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return frags.size();
        }
    };
    private EditText nameText;
    private EditText idText;
    private RadioButton maleButton;
    private EditText nationText;
    private EditText birthdayText;
    private EditText addressText;
    private EditText issueText;
    private EditText validityDateFromText;

    private boolean isValidInputs = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect);

        initView();

        // ActionBar is initiated
        actionBar = getActionBar();

        // Declaration a ViewPager
        vp = (ViewPager) findViewById(R.id.collect);
        //vp.setOffscreenPageLimit(2);

        // Tell the ActionBar we want to use Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab idcardTab = actionBar.newTab().setText("身份证");
        ActionBar.Tab otherTab = actionBar.newTab().setText("其他信息");

        // set the Tab listener. Now we can listen for clicks
        idcardTab.setTabListener(this);
        otherTab.setTabListener(this);

        // add the two tabs to the action bar
        actionBar.addTab(idcardTab);
        actionBar.addTab(otherTab);

        // Set a adapter for ViewPager
        vp.setAdapter(pagerAdapter);

        vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    // if move to other info page, need input the id and name first
                    isValidInputs = validateIdCardInputs();
                    if (!isValidInputs) {
                        position = 0;
                        vp.setCurrentItem(position);
                    }
                }
                actionBar.setSelectedNavigationItem(position);
            }
        });
    }

    private void initView() {
        frags = new ArrayList<Fragment>();

        // create the 2 fragments to display content
        idCard = new FragmentIDCard();
        otherInfo = new FragmentOtherInfo();

        frags.add(idCard);
        frags.add(otherInfo);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        if (!isValidInputs) {
            vp.setCurrentItem(0);
            actionBar.setSelectedNavigationItem(0);
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    private boolean validateIdCardInputs() {
        if (idCard != null && idCard.getView() != null){
            Log.d(TAG, "test---" + idCard.getView());
            if (idText == null) {
                idText = (EditText) idCard.getView().findViewById(R.id.idText);
            }
            if (nameText == null) {
                nameText = (EditText) idCard.getView().findViewById(R.id.nameText);
            }
            if (maleButton == null) {
                maleButton = (RadioButton) idCard.getView().findViewById(R.id.maleButton);
            }
            if (nationText == null) {
                nationText = (EditText) idCard.getView().findViewById(R.id.nationText);
            }
            if (birthdayText == null) {
                birthdayText = (EditText) idCard.getView().findViewById(R.id.birthdayText);
            }
            if (addressText == null) {
                addressText = (EditText) idCard.getView().findViewById(R.id.addressText);
            }
            if (issueText == null) {
                issueText = (EditText) idCard.getView().findViewById(R.id.issueText);
            }
            if (validityDateFromText == null) {
                validityDateFromText = (EditText) idCard.getView().findViewById(R.id.validityDateFrom);
            }
            String id = "";
            String name = "";
            String sex = "";
            String nation = "";
            Date birthday = null;
            String address = "";
            String issue = "";
            Date validityDateFrom = null;
//        Date validityDateTo = null;
            if (idText != null) {
                id = idText.getText().toString();
            }
            if (nameText != null) {
                name = nameText.getText().toString();
            }
            if (maleButton != null && !maleButton.isSelected()) {
                sex = "男";
            } else {
                sex = "女";
            }
            if (nationText != null) {
                nation = nationText.getText().toString();
            }
            if (birthdayText != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    birthday = dateFormat.parse(birthdayText.getText().toString());
                } catch (ParseException pe) {
                    Log.d(TAG, "unexpected date format: " + birthdayText.getText().toString()
                            + " , should be 'yyyy/MM/dd'");
                    //TODO: need should error message here
                }
            }
            if (addressText != null) {
                address = addressText.getText().toString();
            }
            if (issueText != null) {
                issue = issueText.getText().toString();
            }
            if (validityDateFromText != null) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    validityDateFrom = dateFormat.parse(validityDateFromText.getText().toString());
                } catch (ParseException pe) {
                    Log.d(TAG, "unexpected date format: " + validityDateFromText.getText().toString()
                            + " , should be 'yyyy/MM/dd'");
                    //TODO: need should error message here
                }
            }


            if (id.equals("") || name.equals("")) {
                DialogInterface.OnClickListener alertListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (nameText != null) {
                            nameText.requestFocus();
                        }
                    }
                };
                Helper.showErrorMessage(CollectActivity.this, "错误", "必须输入姓名和身份证号码.",
                        null, alertListener);
                return false;
            } else {
                Borrower borrower = new Borrower();
                borrower.setId(id);
                borrower.setName(name);
                borrower.setSex(sex);
                borrower.setNation(nation);
                borrower.setBirthday(birthday);
                borrower.setAddress(address);
                borrower.setIssue(issue);
                borrower.setValidityDateFrom(validityDateFrom);
                borrower.setValidityDateTo(validityDateFrom); //TODO need validatyDateTo
                getIntent().putExtra(Constant.INTENT_KEY_ID, id);
                getIntent().putExtra(Constant.INTENT_KEY_NAME, name);
                saveIDInfo(borrower);
                return true;
            }
        } else {
            return false;
        }
    }

    private boolean saveIDInfo(Borrower borrower) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String rootDir = prefs.getString(PreferencesActivity.KEY_STORAGE_DIR,
                Constant.DEFAULT_STORAGE_DIR);
        String subfolder = Helper.getMD5String(borrower.getName() + borrower.getId());
        String idFile = rootDir + File.separator + subfolder + File.separator + "id.json";
        boolean result = false;
        try {
            result = Helper.saveFile(idFile, borrower.toJson().toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Log.d(TAG, "Fail to save id file " + ex.getMessage());
        }
        return result;
    }
}
