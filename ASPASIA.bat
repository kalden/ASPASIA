@echo off
set SETTINGS=%2
set METHOD=%1

IF %METHOD%==l (
	java -jar ASPASIA.jar "l" %SETTINGS%
) ELSE (
	IF %METHOD%==r (
		java -jar ASPASIA.jar "r" %SETTINGS%
	) ELSE (
		IF %METHOD%==e (
			java -jar ASPASIA.jar "e" %SETTINGS%
		) ELSE (
			IF %METHOD%==s (
				java -jar ASPASIA.jar "s" %SETTINGS%
			)
		)
	)		
)
