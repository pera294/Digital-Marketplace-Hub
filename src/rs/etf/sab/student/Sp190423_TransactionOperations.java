/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import rs.etf.sab.operations.TransactionOperations;

public class Sp190423_TransactionOperations implements TransactionOperations{

    @Override
    public BigDecimal getBuyerTransactionsAmmount(int i) {
        Connection conn = DB.getInstance().getConnection();
        BigDecimal sum = new BigDecimal("0");
        String query = "select * from Transakcija where IdKupac= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
               
                while(rs.next()){
                    sum= sum.add(  new  BigDecimal (rs.getDouble("Novac")+rs.getDouble("Sistem")));
                }
                
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            return new BigDecimal("-1");
        }
        return sum.setScale(3);
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int i) {
         String query = "select * from Transakcija where IdProdavnica =?";
        double sum =0;
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            while (rs.next()) {
                sum+= rs.getDouble("Novac");
            }
           
                return new BigDecimal(sum).setScale(3);
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new BigDecimal(-1);
    }

    @Override
    public List<Integer> getTransationsForBuyer(int i) {
         List<Integer> list=new ArrayList<Integer>();
        String query = "select * from Transakcija where IdKupac =?";
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            while (rs.next()) {
                list.add(rs.getInt("IdTransakcija"));
              
            }
            if(list.isEmpty()) return null;
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int getTransactionForBuyersOrder(int i) {
       String query = "select * from Transakcija where IdPorudzbina =?";
    
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
                return rs.getInt("IdTransakcija");
            }
           

            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int i, int i1) {
        String query = "select * from Transakcija where IdPorudzbina =? and IdProdavnica =?";
    
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
                ps.setInt(2,i1);
               
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
                return rs.getInt("IdTransakcija");
            }
           

            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int i) {
        List<Integer> list=new ArrayList<Integer>();
        String query = "select * from Transakcija where IdProdavnica =?";
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            while (rs.next()) {
                list.add(rs.getInt("IdTransakcija"));
              
            }
            if(list.isEmpty()) return null;
            return list;
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Calendar getTimeOfExecution(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Transakcija where Idtransakcija= ?";
        Calendar calendar = Calendar.getInstance();
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate("Datum");
                    if(date!= null){
                        calendar.setTimeInMillis(date.getTime());
                        return calendar;
                    }
                }
                else return null;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Transakcija t left join Porudzbina p on t.idPorudzbina = p.IdPorudzbina where IdPorudzbina= ?";
        Calendar calendar = Calendar.getInstance();
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal sum = new BigDecimal (rs.getDouble("UkupnaCena"));
                }
                else return null;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
      
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int i, int i1) {
         String query = "select * from Transakcija where IdProdavnica =? and IdPorudzbina = ?";
        double sum =0;
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
                sum+= rs.getDouble("Novac");
            }
           
                return new BigDecimal(sum).setScale(3);
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getTransactionAmount(int i) {
        String query = "select * from Transakcija where IdTransakcija =?";
        double sum =0;
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,i);
               
                ResultSet rs = ps.executeQuery();
                
            if (rs.next()) {
                sum+= rs.getDouble("Novac") + rs.getDouble("Sistem");
            }
           
                return new BigDecimal(sum).setScale(3);
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new BigDecimal(-1);
    }

    @Override
    public BigDecimal getSystemProfit() {
        String query = 
        "select coalesce (sum(Sistem),0) from Transakcija t left join Porudzbina p on t.IdPorudzbina = p.IdPorudzbina where p.stanje = 'arrived'";
        BigDecimal sum = new BigDecimal("0");
         Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ResultSet rs = ps.executeQuery();
                
               
            if (rs.next()) {
               sum = rs.getBigDecimal(1).setScale(3);
               return  sum.setScale(3);
            }
           
            return new BigDecimal(0).setScale(3);
       
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new BigDecimal(-1);
    }
    
    
    public  static int createTransactionBuyerSystem(int idBuyer,int idOrder,double amount,double system){
        
        Connection conn = DB.getInstance().getConnection();
        String query = "insert into Transakcija (IdKupac,IdPorudzbina,Sistem,Novac,Datum) values(?,?,?,?,?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idBuyer);
            ps.setInt(2, idOrder);
            ps.setDouble(3,system);
            ps.setDouble(4,amount);
            ps.setDate(5,new Date (Sp190423_GeneralOperations.current_time.getTimeInMillis()));
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
              return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
}
