package com.sead.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.sead.demand.Adapters.DemandTypeAdapter;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.Department;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DemandTypeActivity extends AppCompatActivity {
    public String TAG = getClass().getSimpleName();
    private DemandTypeActivity mActivity;
    private Spinner mDepartmentSpinner;
    private RecyclerView mDemandTypeList;
    private ProgressDialog mPDDepartment;
    private FetchDepartmentsTask mFetchDepartmentsTask;
    private FetchDemandTypesTask mFetchDemandsTask;
    private ArrayList<Department> mDepartmentsArray;
    private ArrayList<DemandType> mDemandTypesArray;
    private LinearLayoutManager mLayoutManager;
    private DemandTypeAdapter mDemandTypeAdapter;
    private ProgressBar mProgressBar;
    private SparseBooleanArray mSelectedItems;
    private SparseArray<DemandType> mSelectedDemandTypes;
    private SparseArray<DemandType> mDefinitiveSelectedDemandTypes;
    private Button mDoneBtn;
    private DesignateDemandTypesTask mDesignateDemandTypesTask;
    private User mUser;
    private FetchDemandTypesByUserTask mFetchDemandTypesByUserTask;
    private ArrayList<Integer> mDemandTypesIds;

    public DemandTypeActivity() {
        this.mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demand_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.demand_type_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mUser = (User) intent.getExtras().get("user");
        if (mUser == null) finish();
        Log.d(TAG, "User:" + mUser.toString());

        mSelectedItems = new SparseBooleanArray();
        mSelectedDemandTypes = new SparseArray<>();

        mProgressBar = (ProgressBar) findViewById(R.id.demand_type_progress_bar);

        mPDDepartment = new ProgressDialog(this);

        mDoneBtn = (Button) findViewById(R.id.demand_type_btn);

        mDepartmentSpinner = (Spinner) findViewById(R.id.demand_type_department_spinner);

        mDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "department selected:" + position);
                /** Accumulate selected items from all department before changing department and confirming **/
                if (mDemandTypeAdapter != null) {
                    updateSelectedItems(mDemandTypeAdapter.getSelectedIds()); //update selected or not
                    updateSelectedItems(mDemandTypeAdapter.getSelectedDemandTypes()); //update selected or not
                    showSelectedItems();
                    Log.d(TAG, "Sparse to String:" + mSelectedItems.toString());
                }
                fetchDemandTypes(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDemandTypeList = (RecyclerView) findViewById(R.id.demand_type_list);
        mLayoutManager = new LinearLayoutManager(this);
        mDemandTypeList.setLayoutManager(mLayoutManager);

        mDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelectedItems(mDemandTypeAdapter.getSelectedIds()); //update selected or not
                updateSelectedItems(mDemandTypeAdapter.getSelectedDemandTypes()); //update selected or not
                showSelectedItems();
                designateActivities();
            }
        });

        fetchDemandTypesIds(mUser);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("user", mUser);
                intent.putExtra("mode", "ADMIN");
                Log.d(TAG, "array before going back:" + mDefinitiveSelectedDemandTypes.toString());
                intent.putExtra("selected_types", sparseArrayToJson(mDefinitiveSelectedDemandTypes));
                setResult(RESULT_OK,intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("user", mUser);
        intent.putExtra("mode", "ADMIN");
        Log.d(TAG, "array before back pressed:" + mDefinitiveSelectedDemandTypes.toString());
        intent.putExtra("selected_types", sparseArrayToJson(mDefinitiveSelectedDemandTypes));
        setResult(RESULT_OK,intent);
    }

    private void designateActivities() {
        if (CommonUtils.isOnline(this)) {
            if (mDesignateDemandTypesTask == null) {
                try {
                    mDesignateDemandTypesTask = new DesignateDemandTypesTask(mSelectedItems, mUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mDesignateDemandTypesTask.execute();
            }
        } else {
            Snackbar.make(mDoneBtn, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void updateSelectedItems(SparseBooleanArray selectedItems) {
        mSelectedItems = selectedItems;
        /*
        if (selectedItems.size() > 0) {
            for (int i = 0; i < selectedItems.size(); i++) {
                mSelectedItems.put(selectedItems.keyAt(i),selectedItems.valueAt(i));
            }
        }*/
    }

    private void updateSelectedItems(SparseArray<DemandType> selectedTypes) {
        mSelectedDemandTypes = selectedTypes;
        /*
        if (selectedTypes.size() > 0) {
            for (int i = 0; i < selectedTypes.size(); i++) {
                mSelectedDemandTypes.put(selectedTypes.keyAt(i),selectedTypes.valueAt(i));
            }
        }
        */
    }

    private void showSelectedItems() {
        for (int i = 0; i < mSelectedDemandTypes.size(); i++) {
            Log.d(TAG, "id:" + mSelectedDemandTypes.keyAt(i) + " value:" + mSelectedDemandTypes.valueAt(i).toString());
        }
    }

    private void fetchDepartments() {
        if (CommonUtils.isOnline(this)) {
            if (mFetchDepartmentsTask == null) {
                mFetchDepartmentsTask = new FetchDepartmentsTask();
                mFetchDepartmentsTask.execute();
            }
        } else {
            Snackbar.make(mDepartmentSpinner, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void fetchDemandTypes(int position) {
        long departmentId = mDepartmentsArray.get(position).getId();
        Log.d(TAG, "Department id selected: " + departmentId);

        if (CommonUtils.isOnline(this)) {
            if (mFetchDemandsTask == null) {
                mFetchDemandsTask = new FetchDemandTypesTask(departmentId);
                mFetchDemandsTask.execute();
            }
        } else {
            Snackbar.make(mDepartmentSpinner, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    // fetch users activities already designated to him.
    private void fetchDemandTypesIds(User user) {
        if (CommonUtils.isOnline(this)) {
            if (mFetchDemandTypesByUserTask == null) {
                mFetchDemandTypesByUserTask = new FetchDemandTypesByUserTask(user);
                mFetchDemandTypesByUserTask.execute();
            }
        } else {
            Snackbar.make(mDepartmentSpinner, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private String sparseBooleanArrayToJson(SparseBooleanArray sparseBooleanArray) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("key", sparseBooleanArray.keyAt(i));
            jsonObject.put("value", sparseBooleanArray.valueAt(i));
            jsonArray.put(jsonObject);
            Log.d(TAG, "id:" + sparseBooleanArray.keyAt(i) + " value:" + sparseBooleanArray.valueAt(i));
        }
        Log.d(TAG, "Json Array created:" + jsonArray.toString());
        return jsonArray.toString();
    }

    private String sparseArrayToJson(SparseArray<DemandType> sparseArray) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject;
            for (int i = 0; i < sparseArray.size(); i++) {
                jsonObject = new JSONObject();
                jsonObject.put("id", sparseArray.keyAt(i));
                jsonObject.put("title", sparseArray.valueAt(i).getTitle());
                jsonArray.put(jsonObject);
                Log.d(TAG, "id:" + sparseArray.keyAt(i) + " value:" + sparseArray.valueAt(i).toString());
            }
            Log.d(TAG, "Json Array created:" + jsonArray.toString());
            return jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void copySparseArray(SparseArray<DemandType> src, SparseArray<DemandType> dest) {
        dest.clear();
        for (int i = 0; i < src.size(); i++) {
            dest.put(src.keyAt(i), src.valueAt(i));
        }
        Log.d(TAG, "Array copied:" + dest.toString());
    }

    private class FetchDepartmentsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDepartment.setMessage("Carregando setores...");
            mPDDepartment.setCancelable(false);
            mPDDepartment.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            return CommonUtils.POST("/department/all", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mFetchDepartmentsTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    mDepartmentsArray = new ArrayList<>();
                    List<String> departmentsTitles = new ArrayList<>();
                    JSONArray jsonDepartments = jsonObject.getJSONArray("departments");
                    for (int i = 0; i < jsonDepartments.length(); i++) {
                        Department department = Department.build(jsonDepartments.getJSONObject(i));
                        mDepartmentsArray.add(department);
                        departmentsTitles.add(department.getTitle());
                    }

                    if (!departmentsTitles.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, R.layout.autocomplete_custom_item, R.id.autocomplete_item, departmentsTitles);
                        mDepartmentSpinner.setAdapter(adapter);
                        if (!mDepartmentSpinner.isEnabled()) mDepartmentSpinner.setEnabled(true);
                    } else {
                        Snackbar.make(mDepartmentSpinner, "Não há setores disponíveis!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        if (mDepartmentSpinner.isEnabled()) mDepartmentSpinner.setEnabled(false);
                    }
                } else {
                    throw new JSONException("Success hit false");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mPDDepartment.isShowing()) {
                mPDDepartment.dismiss();
            }
        }
    }

    private class FetchDemandTypesTask extends AsyncTask<Void, Void, String> {
        private long departmentId;

        public FetchDemandTypesTask(long position) {
            this.departmentId = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!mProgressBar.isEnabled()) {
                mDemandTypeList.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("department_id", departmentId);
            return CommonUtils.POST("/demandtype/get-by-department", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mFetchDemandsTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    JSONArray demandTypes = jsonObject.getJSONArray("demandtypes");
                    List<String> demandTypesTitles = new ArrayList<>();
                    mDemandTypesArray = new ArrayList<>();
                    for (int i = 0; i < demandTypes.length(); i++) {
                        DemandType demandType = DemandType.build(demandTypes.getJSONObject(i));
                        mDemandTypesArray.add(demandType);
                        demandTypesTitles.add(demandType.getTitle());
                    }

                    if (demandTypesTitles.isEmpty()) {
                        Snackbar.make(mDemandTypeList,"Não há demandas disponíveis nesse setor!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }

                    mDemandTypeAdapter = new DemandTypeAdapter(mDemandTypesArray, mSelectedDemandTypes, mSelectedItems, mActivity);
                    mDemandTypeList.setAdapter(mDemandTypeAdapter);
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mProgressBar.isEnabled()) {
                mProgressBar.setVisibility(View.GONE);
                mDemandTypeList.setVisibility(View.VISIBLE);
            }
        }
    }

    private class DesignateDemandTypesTask extends AsyncTask<Void, Void, String> {
        private User user;
        private String typesJsonArrayString;

        public DesignateDemandTypesTask(SparseBooleanArray sparseBooleanArray, User user) throws JSONException {
            this.user = user;
            this.typesJsonArrayString = sparseBooleanArrayToJson(sparseBooleanArray);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDepartment.setMessage("Atribuindo atividades...");
            mPDDepartment.setCancelable(false);
            mPDDepartment.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("user_id", user.getId());
            values.put("types_array", typesJsonArrayString);
            return CommonUtils.POST("/demandtype/designate", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "Response:" + s);

            mDesignateDemandTypesTask = null;

            if (mPDDepartment.isShowing()) {
                mPDDepartment.dismiss();
            }

            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    JSONArray jsonArray = jsonObject.getJSONArray("demand_types");
                    mSelectedDemandTypes = new SparseArray<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject demandTypeJson = jsonArray.getJSONObject(i);
                        DemandType demandType = DemandType.build(demandTypeJson);
                        mSelectedDemandTypes.put((int) demandType.getId(), demandType);
                    }
                    copySparseArray(mSelectedDemandTypes, mDefinitiveSelectedDemandTypes);
                    Snackbar.make(mDoneBtn,"Concluído.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent();
                    intent.putExtra("user", mUser);
                    intent.putExtra("mode", "ADMIN");
                    intent.putExtra("selected_types", sparseArrayToJson(mDefinitiveSelectedDemandTypes));
                    setResult(RESULT_OK,intent);
                    finish();
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class FetchDemandTypesByUserTask extends AsyncTask<Void, Void, String> {
        private User user;

        public FetchDemandTypesByUserTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDepartment.setMessage("Recebendo atividades do usuário...");
            mPDDepartment.setCancelable(false);
            mPDDepartment.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("user_id", user.getId());
            return CommonUtils.POST("/demandtype/get-by-user", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "Response:" + s);
            mFetchDemandTypesByUserTask = null;

            try {
                JSONObject jsonObject;
                jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    JSONArray demandTypesJson = jsonObject.getJSONArray("demand_types");
                    mDemandTypesArray = new ArrayList<>();
                    for (int i=0; i<demandTypesJson.length(); i++) {
                        JSONObject demandTypeJson = demandTypesJson.getJSONObject(i);
                        DemandType demandType = DemandType.build(demandTypeJson);
                        mDemandTypesArray.add(demandType);
                        mSelectedItems.put((int) demandType.getId(), true);
                        mSelectedDemandTypes.put((int) demandType.getId(), demandType);
                    }
                    mDefinitiveSelectedDemandTypes = new SparseArray<>();
                    copySparseArray(mSelectedDemandTypes, mDefinitiveSelectedDemandTypes);
                    fetchDepartments();
                } else {
                    throw new JSONException("success hit false");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



}
