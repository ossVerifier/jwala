@echo off
for /D %%a in (*.*) do if exist %%a\logs del /Q %%a\logs\*.*
for /D %%a in (*.*) do if exist %%a\temp del /S /Q %%a\temp\*.*
for /D %%a in (*.*) do if exist %%a\work del /S /Q %%a\work\*.*