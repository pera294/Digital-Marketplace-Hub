/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import rs.etf.sab.operations.CityOperations;
public class Sp190423_CityOperations implements CityOperations {

    @Override
    public int createCity(String string) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Grad (Naziv) values(?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //System.out.println("napravio grad");
                //System.out.println(rs.getInt(1));
                return rs.getInt(1);
            }
           
             //System.out.println(" nije napravio grad");
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getCities() {
        List<Integer> list=new ArrayList<Integer>();
         Connection conn = DB.getInstance().getConnection();
        try (
            Statement stmt = conn.createStatement();  
            ResultSet rs = stmt.executeQuery("select * from Grad")) {
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                list.add(rs.getInt("IdGrad"));
              
            }
            if(list.isEmpty()) return null;
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int connectCities(int i, int i1, int i2) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into PovezaniGradovi (idGrad1,idGrad2,RazdaljinaDani) values(?,?,?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.setInt(3, i2);
             
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
               // System.out.println("povezao gradove");
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getConnectedCities(int i) {
        List<Integer> list=new ArrayList<Integer>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from PovezaniGradovi";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            //stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if(rs.getInt(1)== i){
                        list.add(rs.getInt(2));
                    }
                    if(rs.getInt(2)== i){
                        list.add(rs.getInt(1));
                    }
                    
                }
                if(list.isEmpty()) return null;
                return (List<Integer>)list;
                
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getShops(int i) {
        List<Integer> list=new ArrayList<Integer>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Prodavnica where idGrad= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                list.add(rs.getInt("IdProdavnica"));
                    //System.out.println(rs.getInt("IdProdavnica"));
                }
                return (List<Integer>)list;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
}
