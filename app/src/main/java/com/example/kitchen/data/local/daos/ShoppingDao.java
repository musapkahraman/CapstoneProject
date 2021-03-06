/*
 * Reference
 * https://stackoverflow.com/questions/45677230/android-room-persistence-library-upsert/48641762#48641762
 */

package com.example.kitchen.data.local.daos;

import com.example.kitchen.data.local.entities.Ware;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ShoppingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Ware ware);

    @Update
    void update(Ware ware);

    @Delete
    void delete(Ware ware);

    @Query("SELECT * from shopping_list")
    LiveData<List<Ware>> getShoppingList();

}
