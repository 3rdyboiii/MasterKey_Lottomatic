package com.example.masterkey_luckythree.helper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyViewModel extends ViewModel {

    private final MutableLiveData<Boolean> dataLoaded = new MutableLiveData<>();

    public LiveData<Boolean> getDataLoaded() {
        return dataLoaded;
    }

    /*public void loadData(Context context) {
        // Inflate the loading screen layout
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.loading_screen, null);
        builder.setView(layout_dialog);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Simulate data loading
        new Handler().postDelayed(() -> {
            // Once data is loaded
            dataLoaded.postValue(true);
            // Dismiss the dialog
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2000); // Simulate 2 seconds delay
    }*/
}

