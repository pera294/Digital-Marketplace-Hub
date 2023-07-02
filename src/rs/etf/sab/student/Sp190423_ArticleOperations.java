/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import rs.etf.sab.operations.ArticleOperations;

public class Sp190423_ArticleOperations implements ArticleOperations{

    @Override
    public int createArticle(int i, String string, int i1) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Artikal (Naziv,IdProdavnica,Cena) values(?,?,?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string);
            ps.setInt(2, i);
            ps.setInt(3, i1);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //System.out.println("napravio grad");
                //System.out.println(rs.getInt(1));
                return rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public static double getPriceArticle(int idArticle){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Artikal a left join Prodavnica p on a.idProdavnica = p.IdProdavnica where IdArtikal= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idArticle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Cena")*(100 - rs.getInt("Popust")*1.0)/100;
            }
               
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }
    
    public static int getAmount(int idArticle){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Artikal where IdArtikal= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idArticle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("Kolicina");
            }
               
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }
    
      public static void setAmount(int idArticle, int amount){
        Connection conn = DB.getInstance().getConnection();
        String query = "Update Artikal set Kolicina = ? where IdArtikal= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, amount);
            ps.setInt(2, idArticle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return;
            }
               
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return;
    }
    
}
