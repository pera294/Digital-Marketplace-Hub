/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;


import rs.etf.sab.operations.ShopOperations;

public class Sp190423_ShopOperations implements ShopOperations {
    
    @Override
    public int createShop(String string, String string1) {
        int idGrad = getCityId(string1);
        if(idGrad < 0){
            return -1;
        }
        
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Prodavnica (Naziv,idGrad) values(?, ?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string);
            ps.setInt(2, idGrad);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
              return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int setCity(int i, String string) {
        int idGrad = getCityId(string);
        if(idGrad < 0){
            return -1;
        }
       Connection conn = DB.getInstance().getConnection();
        String query = "select * from Prodavnica";
        try (
                 PreparedStatement stmt = conn.prepareStatement(query,
                 ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                 ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                rs.updateInt("idGrad", idGrad);
                rs.updateRow();
                return 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int getCity(int i) {
         Connection conn = DB.getInstance().getConnection();
        String query = "select * from Prodavnica where IdProdavnica= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("IdGrad");
                }
                else return 0;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return 0;
    }
    
    @Override
    public int setDiscount(int i, int i1) {
        
       Connection conn = DB.getInstance().getConnection();
          String query = "update Prodavnica set popust = ? where idProdavnica= ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, i1);
                ps.setInt(2, i);
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
                return 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int increaseArticleCount(int i, int i1) {
        int kolicina = getArticleCount(i);
          Connection conn = DB.getInstance().getConnection();
          String query = "update Artikal set Kolicina = ? where idArtikal= ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, kolicina+i1);
                ps.setInt(2, i);
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
               return kolicina+i1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
        
    }

    @Override
    public int getArticleCount(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Artikal where IdArtikal= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Kolicina");
                }
                else return 0;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return 0;
       
    }

    @Override
    public List<Integer> getArticles(int i) {
       List<Integer> list=new ArrayList<Integer>();
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Artikal where idProdavnica= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                list.add(rs.getInt("IdArtikal"));
                    
                }
                if(list.isEmpty()) return null;
                return (List<Integer>)list;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int getDiscount(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Prodavnica where IdProdavnica= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Popust");
                }
                else return 0;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return 0;
    }
    
    public int getCityId(java.lang.String cityName){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Grad where Naziv= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cityName);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("IdGrad");
                }
                else return -1;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }    
}
