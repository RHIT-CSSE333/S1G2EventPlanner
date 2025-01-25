EXEC dbo.ShowAvailableEvents
SELECT * FROM Event

SELECT dbo.EventAvailableForPublic(2)  -- False
SELECT dbo.EventAvailableForPublic(7)  -- False
SELECT dbo.EventAvailableForPublic(8)  -- False
SELECT dbo.EventAvailableForPublic(9)  -- True
SELECT dbo.EventAvailableForPublic(10)  -- True