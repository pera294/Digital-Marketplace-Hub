/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import rs.etf.sab.operations.GeneralOperations;

public class Sp190423_GeneralOperations implements GeneralOperations{
    
    public static Calendar current_time;

    @Override
    public void setInitialTime(Calendar clndr) {
       current_time =(Calendar) clndr.clone();
    }

    @Override
    public Calendar time(int i) {
        for(int cnt =0; cnt<i; cnt ++ ){
             current_time.add(Calendar.DAY_OF_MONTH, 1);
             Sp190423_OrderOperations.PassTime();
        }
     
      return current_time;
    }

    @Override
    public Calendar getCurrentTime() {
        return current_time;
    }

    @Override
    public void eraseAll() {
        Connection connection = DB.getInstance().getConnection();
        String query;
        try (
           Statement statement = connection.createStatement()) {
            
           query = "delete Transakcija";
           statement.executeUpdate(query);
           query = "delete Stavka";
           statement.executeUpdate(query);
           query = "delete Porudzbina";
           statement.executeUpdate(query);
           query = "delete Artikal";
           statement.executeUpdate(query);
           query = "delete Kupac";
           statement.executeUpdate(query);
           query = "delete Prodavnica";
           statement.executeUpdate(query);
           query = "delete PovezaniGradovi";
           statement.executeUpdate(query);
           query = "delete Grad";
           statement.executeUpdate(query);
           
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
