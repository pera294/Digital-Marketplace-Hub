/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.util.List;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.etf.sab.operations.BuyerOperations;

public class Sp190423_BuyerOperations implements BuyerOperations{

    @Override
    public int createBuyer(String string, int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Kupac (Ime,GradKupca) values(?,?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string);
            ps.setInt(2, i);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //System.out.println("napravio kupca");
                //System.out.println(rs.getInt(1));
                return rs.getInt(1);
            }
             //System.out.println(" nije napravio kupca");
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int setCity(int i, int i1) {
         Connection conn = DB.getInstance().getConnection();
          String query = "update Kupac set GradKupca = ? where idKupac= ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, i1);
                ps.setInt(2, i);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return 1;
                }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int getCity(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Kupac where IdKupac= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("GradKupca");
                }
                
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public BigDecimal increaseCredit(int i, BigDecimal bd) {
        BigDecimal novac = getCredit(i);
        BigDecimal vrednost = novac.add(bd);
        Connection conn = DB.getInstance().getConnection();
        String query = "update Kupac set Novac = ? where IdKupac= ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setBigDecimal(1,vrednost);
                ps.setInt(2, i);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   return vrednost.setScale(3);
                }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int createOrder(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Porudzbina (IdKupac) values(?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //System.out.println("napravio grad");
                //System.out.println(rs.getInt(1));
                return rs.getInt(1);
            }
      
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getOrders(int i) {
        List<Integer> list=new ArrayList<Integer>();
        String query = "select * from Porudzbina where IdKupac =?";
        Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1,i);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    list.add(rs.getInt("IdPorudzbina"));
                }
                if(list.isEmpty()) return null;
                return list;
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public BigDecimal getCredit(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Kupac where IdKupac= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("Novac").setScale(3);
                }
                
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }
    
}
