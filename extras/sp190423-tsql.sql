USE [SAB_Projekat]
GO
/****** Object:  Trigger [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]    Script Date: 03-Jun-23 3:39:01 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER TRIGGER [dbo].[TR_TRANSFER_MONEY_TO_SHOPS]
ON [dbo].[Porudzbina]
AFTER UPDATE
AS
BEGIN
    DECLARE @IdProdavnice int;
    DECLARE @IdPorudzbine int;
    DECLARE @Novac Decimal(10,3);
    DECLARE @Datum Date;

    DECLARE @Profit Decimal(10,3);

    IF UPDATE(Stanje) AND EXISTS(SELECT * FROM inserted WHERE Stanje = 'arrived')
    BEGIN
        -- Temporary table to store the relevant IdPorudzbina values from inserted
        CREATE TABLE #TempPorudzbine (IdPorudzbina int);

        -- Populate the temporary table with the IdPorudzbina values
        INSERT INTO #TempPorudzbine (IdPorudzbina)
        SELECT IdPorudzbina
        FROM inserted
        WHERE Stanje = 'arrived'
            AND EXISTS(SELECT * FROM deleted WHERE IdPorudzbina = inserted.IdPorudzbina AND Stanje = 'sent');

        -- Cursor to iterate over unique IdProdavnica values
        DECLARE cursor_prodavnica CURSOR FOR
        SELECT DISTINCT a.IdProdavnica
        FROM Stavka s
        INNER JOIN Porudzbina p ON s.IdPorudzbina = p.IdPorudzbina
        INNER JOIN Artikal a ON s.IdArtikal = a.IdArtikal
        WHERE p.Stanje = 'arrived'
            AND EXISTS(SELECT * FROM deleted WHERE IdPorudzbina IN (SELECT IdPorudzbina FROM #TempPorudzbine) AND Stanje = 'sent');

        OPEN cursor_prodavnica;

        FETCH NEXT FROM cursor_prodavnica INTO @IdProdavnice;

        WHILE @@FETCH_STATUS = 0
        BEGIN
            -- Cursor to iterate over relevant orders for each IdProdavnica
            DECLARE cursor_porudzbina CURSOR FOR
            SELECT tp.IdPorudzbina
            FROM #TempPorudzbine tp
            WHERE EXISTS(SELECT * FROM deleted WHERE IdPorudzbina = tp.IdPorudzbina AND Stanje = 'sent');

            OPEN cursor_porudzbina;

            FETCH NEXT FROM cursor_porudzbina INTO @IdPorudzbine;

            WHILE @@FETCH_STATUS = 0
            BEGIN
                SELECT @Novac = SUM(s.Cena)
                FROM Stavka s
                INNER JOIN Porudzbina p ON s.IdPorudzbina = p.IdPorudzbina
                INNER JOIN Artikal a ON s.IdArtikal = a.IdArtikal
                WHERE p.IdPorudzbina = @IdPorudzbine
                    AND a.IdProdavnica = @IdProdavnice;

                SET @Datum = (
				SELECT p.DatumStiglaDoKupca
				FROM Porudzbina p
				WHERE p.IdPorudzbina = @IdPorudzbine); -- Assign the current date to @Datum variable

                INSERT INTO Transakcija (IdPorudzbina, IdProdavnica, Novac,Sistem, Datum) 
                VALUES (@IdPorudzbine, @IdProdavnice, @Novac,0, @Datum);

                FETCH NEXT FROM cursor_porudzbina INTO @IdPorudzbine;
            END;

            CLOSE cursor_porudzbina;
            DEALLOCATE cursor_porudzbina;

            SELECT @Profit = Profit
            FROM Prodavnica
            WHERE IdProdavnica = @IdProdavnice;

            UPDATE Prodavnica
            SET Profit = @Profit + (
                SELECT SUM(Novac)
                FROM Transakcija
                WHERE IdProdavnica = @IdProdavnice
            )
            WHERE IdProdavnica = @IdProdavnice;

            FETCH NEXT FROM cursor_prodavnica INTO @IdProdavnice;
        END;

        CLOSE cursor_prodavnica;
        DEALLOCATE cursor_prodavnica;

        -- Drop the temporary table
        DROP TABLE #TempPorudzbine;
    END
END;
go
USE [SAB_Projekat]
GO
/****** Object:  StoredProcedure [dbo].[SP_FINAL_PRICE]    Script Date: 03-Jun-23 3:39:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[SP_FINAL_PRICE]
(
    
    @orderId INT,
    @finalPrice DECIMAL(10,3) OUTPUT
)
AS
BEGIN
    DECLARE @totalAmount DECIMAL(10,3);
    DECLARE @discountedAmount DECIMAL(10,3);
    DECLARE @discountRate DECIMAL(4,2);
    DECLARE @hasRecentPurchase BIT;

    select @finalPrice = sum(s.Kolicina * a.Cena * (100-p.Popust*1.0)/100)
	from Stavka s left join artikal a  on s.IdArtikal = a.IdArtikal 
	left join prodavnica p on a.IdProdavnica = p.IdProdavnica
	where s.IdPorudzbina = @orderId

    
    SELECT @finalPrice AS FinalPrice;
END;
