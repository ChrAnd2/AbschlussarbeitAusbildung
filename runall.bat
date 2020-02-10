@echo off
@rem Mit dieser Batchdatei werden nacheinander alle Beispiele aus dem Beispiele Ordner ausgefuehrt


IF EXIST .\Tests\*.* (

		@FOR  %%x in (Tests\*.in) do (
			@echo Bearbeitet: %%x 
			call java -jar .\dist\Netzplanerstellung.jar -t %%x
		)
	) else (
		echo Ordner Tests\ konnte nicht gefunden werden!
	)

pause

