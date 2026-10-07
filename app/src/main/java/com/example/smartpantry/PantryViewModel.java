package com.example.smartpantry;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.PantryItemDao;

import java.util.List;

public class PantryViewModel extends AndroidViewModel{

    private final PantryItemDao dao;
    private final LiveData<List<PantryItem>> pantryItems;

    public PantryViewModel(@NonNull Application application){
        super(application);
        // First call to getInstance() creates the database and triggers this seeding.

        dao = AppDatabase.getInstance(application).pantryItemDao();
        pantryItems = dao.getAll();
    }

    public LiveData<List<PantryItem>> getPantryItems(){
        return pantryItems;
    }

    // Writes must run off the main thread.
    public void insert(PantryItem item){
        AppDatabase.databaseExecutor.execute(() -> dao.insert(item));
    }

    public void update(PantryItem item){
        AppDatabase.databaseExecutor.execute(() -> dao.update(item));
    }

    public void delete(PantryItem item){
        AppDatabase.databaseExecutor.execute(() -> dao.delete(item));
    }
}
