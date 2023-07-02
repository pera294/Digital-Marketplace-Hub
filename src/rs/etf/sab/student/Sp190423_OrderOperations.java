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
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.ArticleOperations;
import rs.etf.sab.operations.BuyerOperations;
import rs.etf.sab.operations.OrderOperations;

public class Sp190423_OrderOperations implements OrderOperations{

    @Override
    public int addArticle(int i, int i1, int i2) {
        
        Connection conn = DB.getInstance().getConnection();
        double cenaArtikla= Sp190423_ArticleOperations.getPriceArticle(i1);
        int amount  = Sp190423_ArticleOperations.getAmount(i1);
        if(amount< i2){
            return -1; //nema dovoljno
        }
        
        Sp190423_ArticleOperations.setAmount(i1, amount-i2);
        
        String query = "insert into Stavka (IdPorudzbina,IdArtikal,Kolicina,Cena) values(?,?,?,?)";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.setInt(3, i2);
            ps.setDouble(4, 0.95 * i2 * cenaArtikla);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public int removeArticle(int i, int i1) {
        Connection conn = DB.getInstance().getConnection();
        String query = "delete from Stavka where IdPorudzbina = ? and IdArtikal = ?";
        try ( PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ps.setInt(2, i1);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return 1;
            }
           
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    @Override
    public List<Integer> getItems(int i) {
        List<Integer> list=new ArrayList<Integer>();
        String query = "select * from Stavka where IdPorudzbina =?";
        Connection conn = DB.getInstance().getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1,i);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    list.add(rs.getInt("IdStavka")); 
                }
                return list;
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int completeOrder(int i) {
        Sp190423_BuyerOperations bo = new Sp190423_BuyerOperations();
        int discount;
        int pdv = 5;
        
        //provera da li ima dodatnog popusta 
        if (checkSumOfOrders(getBuyer(i))){
            discount = 2;
            pdv = 3;
        }
        else discount = 0;
        
        //update cena za porudzbinu
        double ukupnaCena = getFinalPrice(i).doubleValue()*(100-discount*1.0)/100;
        
        setUkupnaCena(i, ukupnaCena, pdv);
        //System.out.println("popust za porudzbinu je " + discount );
        
        
        //provera jel kupac ima dovoljno para
        Double credit = (bo.getCredit(getBuyer(i))).doubleValue();
        if(credit < ukupnaCena ){
            //System.out.println("nema para nema novaca");
            return -1;
        }
        
        //skidanje para sa racuna kupca 
        bo.increaseCredit(getBuyer(i), new BigDecimal(ukupnaCena).negate());
        
        //upisi transakcije  
        Sp190423_TransactionOperations to = new Sp190423_TransactionOperations();
        to.createTransactionBuyerSystem(getBuyer(i), i,  (100-pdv*1.0)/100 *ukupnaCena,ukupnaCena - (100-pdv*1.0)/100 *ukupnaCena);
        
        int IdCityCheckpoint = findCheckPoint(i);
        
        Connection conn = DB.getInstance().getConnection();
        String query = "update Porudzbina set stanje = ?,DatumPotvrdjena = ?,TrenutnaLokacija =?,Preostalo = ? where idPorudzbina= ?";
           
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, "sent");
                ps.setDate(2,new Date (Sp190423_GeneralOperations.current_time.getTimeInMillis()));
                ps.setInt(3, IdCityCheckpoint);
                ps.setInt(4, calculateTimeToAssemble(i,IdCityCheckpoint));
                ps.setInt(5, i);
                
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
    public BigDecimal getFinalPrice(int i) {
        Connection conn = DB.getInstance().getConnection();
        BigDecimal finalPrice =null;
        String query = "{ call SP_FINAL_PRICE (?,?) }";
        try (CallableStatement cs = conn.prepareCall(query)) {
            cs.setInt(1, i);
            cs.registerOutParameter(2, java.sql.Types.DECIMAL);
            cs.execute();

            finalPrice = cs.getBigDecimal(2).setScale(3);
            //System.out.println("Final Price: " + finalPrice);
    } catch (SQLException ex) {
        ex.printStackTrace();
        }
        return finalPrice.setScale(3);
    }

    @Override
    public BigDecimal getDiscountSum(int i) {
        BigDecimal discount = new BigDecimal("0");
        Connection conn = DB.getInstance().getConnection();
        String query = "select s.Kolicina,a.Cena,p.Popust\n" +
                        "from Stavka s left join Artikal a on s.IdArtikal = a.IdArtikal\n" +
                        "left join Prodavnica p on a.IdProdavnica=p.IdProdavnica\n" +
                        "where s.IdPorudzbina = ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                
                double temp;
                BigDecimal bigtemp;
                
                while (rs.next()) {
                    temp=(rs.getDouble("Popust")/100 * rs.getInt("Kolicina") * rs.getDouble("Cena"));
                    bigtemp = new BigDecimal(temp);
                    discount = discount.add(bigtemp);
                }
                
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return new BigDecimal("-1");
        }
        
        //System.out.println("discount = " + discount);
        return discount.setScale(3);
    }

    @Override
    public String getState(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina where IdPorudzbina= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Stanje");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
           
        }
        return null;
    }

    @Override
    public Calendar getSentTime(int i) {
      Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina where IdPorudzbina= ?";
        Calendar calendar = Calendar.getInstance();
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Date date = rs.getDate("DatumPotvrdjena");
                    if(date!= null){
                        calendar.setTimeInMillis(date.getTime());
                        return calendar;
                    }
                }
                
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return null;
    }

    @Override
    public Calendar getRecievedTime(int i) {
       Connection conn = DB.getInstance().getConnection();
       String query = "select * from Porudzbina where IdPorudzbina= ?";
       Calendar calendar = Calendar.getInstance();
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Date date = rs.getDate("DatumStiglaDoKupca");
                    if(date!= null){
                        calendar.setTimeInMillis(date.getTime());
                        return calendar;
                    }
                }

        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return null;
    }

    @Override
    public int getBuyer(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina where IdPorudzbina= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("IdKupac");
            }
                      
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }

    @Override
    public int getLocation(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina p left join Kupac k on p.IdKupac = k.IdKupac where IdPorudzbina= ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                   
                if(rs.getDate("DatumPoslataDoKupca") == null){
                    return rs.getInt("TrenutnaLokacija");
                }
                  
                List<Integer> path = getPath(rs.getInt("TrenutnaLokacija"), rs.getInt("GradKupca"));
                if(path.size()== 1 ) 
                   return path.get(0);
                    
                int totalDistance = (int)findShortestDistance(rs.getInt("TrenutnaLokacija"), rs.getInt("GradKupca"));
                int temp;
                int ret = path.get(0);
                int remaining = rs.getInt("Preostalo");
                int passed = totalDistance - remaining;

                for (int cnt =0; cnt <path.size(); cnt++){
                    temp = (int)findShortestDistance(path.get(cnt),path.get(cnt+1));
                    if( passed  >= temp){
                        ret = path.get(cnt+1);
                        passed= passed-temp;
                    }
                    else break;
                        
                }
                   
                //System.out.println("trenutna lokacija je " + ret);
                return ret;
                                       
                }
                
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
  
    }
    
    public boolean checkSumOfOrders(int idKorisnik){
        String query = "select * from Porudzbina where IdKupac =?";
        double sum =0;
        Connection conn = DB.getInstance().getConnection();
        try (
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,idKorisnik);
               
            ResultSet rs = ps.executeQuery();
                
            while (rs.next()) {
                sum+= rs.getDouble("UkupnaCena");
            }
            
            if(sum>10000)
                return true;
            
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private void setUkupnaCena(int idPorudzbina,double Ukupnacena, int pdv){
        Connection conn = DB.getInstance().getConnection();
        String query = "update Porudzbina set UkupnaCena = ?  , PDV = ? where idPorudzbina= ?";
        try (
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setDouble(1, Ukupnacena);
                ps.setDouble(2, pdv);
                ps.setInt(3, idPorudzbina);
               
                ResultSet rs = ps.executeQuery();
  
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ;   
    }
    
    public static double findShortestDistance(int cityId1, int cityId2) {
        Connection conn = DB.getInstance().getConnection();

        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT IdGrad1, IdGrad2, RazdaljinaDani FROM PovezaniGradovi";
            ResultSet rs = stmt.executeQuery(query);

            Map<Integer, Map<Integer, Double>> graph = new HashMap<>();
            while (rs.next()) {
                int startCity = rs.getInt("IdGrad1");
                int endCity = rs.getInt("IdGrad2");
                double distance = rs.getDouble("RazdaljinaDani");

                graph.putIfAbsent(startCity, new HashMap<>());
                graph.get(startCity).put(endCity, distance);

                graph.putIfAbsent(endCity, new HashMap<>());
                graph.get(endCity).put(startCity, distance);
            }

            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
            pq.offer(new int[]{cityId1, 0});

            Map<Integer, Double> distances = new HashMap<>();
            distances.put(cityId1, 0.0);

            while (!pq.isEmpty()) {
                int[] curr = pq.poll();
                int currCity = curr[0];
                int currDist = curr[1];

                if (currCity == cityId2) {
                    return distances.get(currCity);
                }

                if (currDist > distances.get(currCity)) {
                    continue;
                }

                if (graph.containsKey(currCity)) {
                    for (int neighbor : graph.get(currCity).keySet()) {
                        double newDist = currDist + graph.get(currCity).get(neighbor);
                        if (!distances.containsKey(neighbor) || newDist < distances.get(neighbor)) {
                            distances.put(neighbor, newDist);
                            pq.offer(new int[]{neighbor, (int) newDist});
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 

        return Double.MAX_VALUE;
    }
    
    public int findCheckPoint(int orderId){
        
        Connection conn = DB.getInstance().getConnection();
        double minimumDistance=Double.MAX_VALUE;
        int idCity=0;
        String query = "select k.IdKupac,k.GradKupca,p.IdPorudzbina,p.DatumPotvrdjena,a.IdArtikal,a.Naziv,pr.IdProdavnica,pr.IdGrad\n" +
                        "from Kupac k left join Porudzbina p on k.IdKupac = p.IdKupac\n" +
                        "left join Stavka s on p.IdPorudzbina = s.IdPorudzbina\n" +
                        "left join Artikal a on s.IdArtikal = a.IdArtikal \n" +
                        "left join Prodavnica pr on a.IdProdavnica= pr.IdProdavnica\n" +
                        "where p.IdPorudzbina = ?";
        
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);
           
            ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                   if(minimumDistance > findShortestDistance(rs.getInt("GradKupca"), rs.getInt("IdGrad"))){
                       minimumDistance = findShortestDistance(rs.getInt("GradKupca"), rs.getInt("IdGrad"));
                       idCity = rs.getInt("IdGrad");
                   }
                }
                
                return idCity;
                
                //System.out.println("checkpoint = " + idCity);
     
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);

        }
        return -1;
        
    }
    
    public int calculateTimeToAssemble(int orderId,int IdCityCheckPoint){
        
        Connection conn = DB.getInstance().getConnection();
        
        double maxtime=0;
        int days=0;
        String query = "select por.TrenutnaLokacija,s.IdPorudzbina,s.IdStavka,a.IdArtikal,p.IdGrad\n" +
                        "from Stavka s  left join Porudzbina por on s.idPorudzbina = por.IdPorudzbina\n" +
                        "left join Artikal a on s.IdArtikal=a.IdArtikal\n" +
                        "left join Prodavnica p on a.IdProdavnica = p.IdProdavnica\n" +
                        "where s.IdPorudzbina= ?";
        
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                   if(maxtime < findShortestDistance(IdCityCheckPoint, rs.getInt("IdGrad"))){
                       maxtime = findShortestDistance(IdCityCheckPoint, rs.getInt("IdGrad"));
                       days = (int)maxtime;
                   }
                }
                
                return days;
                
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return -1;
        
    }
   
    public static int getTimeLeft(int i) {
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina where IdPorudzbina= ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, i);
            try ( ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Preostalo");
                }
                else return -1;
            } catch (SQLException ex) {
                Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return -1;
    }
    
    public static void tryToSendOrderToCustomer(int orderId) {
     Connection conn = DB.getInstance().getConnection();
     String query = "select * from Porudzbina p left join Kupac k on p.IdKupac = k.IdKupac where IdPorudzbina = ?";
     try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
         stmt.setInt(1, orderId);
         try (ResultSet rs = stmt.executeQuery()) {
             if (rs.next()) {
              
                 
                 if (rs.getDate("DatumPoslataDoKupca") == null && rs.getInt("Preostalo") == 0) {
                     rs.updateDate("DatumPoslataDoKupca", new Date(Sp190423_GeneralOperations.current_time.getTimeInMillis()));
                     rs.updateInt("Preostalo", (int) findShortestDistance(rs.getInt("TrenutnaLokacija"), rs.getInt("GradKupca")));
                     rs.updateRow();
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
         }
     } catch (SQLException ex) {
         Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
     }
 }

    public static void tryToArriveToCustomer(int orderId){
        Connection conn = DB.getInstance().getConnection();
        String query = "select * from Porudzbina p left join Kupac k on p.IdKupac = k.IdKupac where IdPorudzbina = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                 
                 


                if (rs.getDate("DatumPoslataDoKupca") != null && rs.getInt("Preostalo") == 0) {
                     rs.updateDate("DatumStiglaDoKupca", new Date(Sp190423_GeneralOperations.current_time.getTimeInMillis()));
                     rs.updateInt("Preostalo", 0);
                     rs.updateInt("TrenutnaLokacija",rs.getInt("GradKupca"));
                     rs.updateString("Stanje", "arrived");
                     rs.updateRow();
                     
                    //upisi transakcije  
                   //Sp190423_TransactionOperations to = new Sp190423_TransactionOperations();
                   //int id = rs.getInt("IdPorudzbina");
                   //double ukupnaCena = rs.getDouble("ukupnaCena");
                   //int pdv = rs.getInt("PDV");
                   //upisi transakcije  
                    //Sp190423_TransactionOperations to = new Sp190423_TransactionOperations();
                    //to.createTransactionBuyerSystem(getBuyer(i), i,  (100-pdv*1.0)/100 *ukupnaCena,ukupnaCena - (100-pdv*1.0)/100 *ukupnaCena);

                    // Sp190423_OrderOperations oo = new Sp190423_OrderOperations();
                   //Sp190423_TransactionOperations.createTransactionBuyerSystem(oo.getBuyer(id), id,  (100-pdv*1.0)/100 *ukupnaCena, ukupnaCena - (100-pdv*1.0)/100 *ukupnaCena);
        
                 }

                 if(rs.getDate("DatumStiglaDoKupca") != null){
                     rs.updateInt("Preostalo", 0);
                     rs.updateRow();
                 }
             }
         } catch (SQLException ex) {
             Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
         }
     } catch (SQLException ex) {
         Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
     }

    }
    
    public static void PassTime() {
     Connection conn = DB.getInstance().getConnection();
     String query = "select * from Porudzbina";
     try (PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
         try (ResultSet rs = ps.executeQuery()) {
             while (rs.next()) {
                //System.out.println("preostalo: " + getTimeLeft(rs.getInt("IdPorudzbina")));
                 int preostalo = getTimeLeft(rs.getInt("IdPorudzbina")) - 1;
                 rs.updateInt("Preostalo", preostalo);
                 rs.updateRow();
                 tryToSendOrderToCustomer(rs.getInt("IdPorudzbina"));
                 tryToArriveToCustomer(rs.getInt("IdPorudzbina"));
             }
         } catch (SQLException ex) {
             Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
         }
     } catch (SQLException ex) {
         Logger.getLogger(Sp190423_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
     }
     return;
 }

    public List<Integer> getPath(int startCity, int destinationCity) {
         List<Integer> path = new ArrayList<>();
               Connection conn = DB.getInstance().getConnection();
               String query = "SELECT IdGrad1, IdGrad2, RazdaljinaDani FROM PovezaniGradovi";
         try  {
             try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                 Map<Integer, Map<Integer, Integer>> graph = new HashMap<>();
                 while (rs.next()) {
                     int city1 = rs.getInt("IdGrad1");
                     int city2 = rs.getInt("IdGrad2");
                     int distance = rs.getInt("RazdaljinaDani");

                     graph.putIfAbsent(city1, new HashMap<>());
                     graph.putIfAbsent(city2, new HashMap<>());

                     graph.get(city1).put(city2, distance);
                     graph.get(city2).put(city1, distance);
                 }

                 Map<Integer, Integer> distanceMap = new HashMap<>();
                 Map<Integer, Integer> previousMap = new HashMap<>();
                 Set<Integer> visited = new HashSet<>();

                 for (int city : graph.keySet()) {
                     distanceMap.put(city, Integer.MAX_VALUE);
                     previousMap.put(city, -1);
                 }

                 distanceMap.put(startCity, 0);

                 while (!visited.contains(destinationCity)) {
                     int minDistance = Integer.MAX_VALUE;
                     int currentCity = -1;

                     for (Map.Entry<Integer, Integer> entry : distanceMap.entrySet()) {
                         int city = entry.getKey();
                         int distance = entry.getValue();

                         if (!visited.contains(city) && distance < minDistance) {
                             minDistance = distance;
                             currentCity = city;
                         }
                     }

                     if (currentCity == -1) {
                         // No path exists
                         return path;
                     }

                     visited.add(currentCity);

                     Map<Integer, Integer> neighbors = graph.get(currentCity);
                     if (neighbors != null) {
                         for (Map.Entry<Integer, Integer> entry : neighbors.entrySet()) {
                             int neighborCity = entry.getKey();
                             int neighborDistance = entry.getValue();
                             int totalDistance = distanceMap.get(currentCity) + neighborDistance;

                             if (totalDistance < distanceMap.get(neighborCity)) {
                                 distanceMap.put(neighborCity, totalDistance);
                                 previousMap.put(neighborCity, currentCity);
                             }
                         }
                     }
                 }

                 // Reconstruct the path
                 int currentCity = destinationCity;
                 while (currentCity != -1) {
                     path.add(0, currentCity);
                     currentCity = previousMap.get(currentCity);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

           //System.out.println(path);

         return path;
     }
    
}
