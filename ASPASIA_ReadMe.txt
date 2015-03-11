To run ASPASIA:
- Make sure the BASH file and the JAR are in the same folder
- Set the BASH script sure that you have permission to execute it.
- Open a command prompt, and run one of the following commands. Note ASPASIA takes two arguments: the technique and the full path to the ASPASIA Settings file. Example settings files are available from the website (www.york.ac.uk/ycil/software/ASPASIA), or you can generate them using the Online ASPASIA Settings File Generator (php.york.ac.uk/ycil/ASPASIA):
./ASPASIA.bash -r [full path to settings file] - creates models for Robustness Analysis
./ASPASIA.bash -l [full path to settings file] - creates models for Latin-Hypercube Analysis
./ASPASIA.bash -e [full path to settings file] - creates models for eFAST Analysis
./ASPASIA.bash -s [full path to settings file] - creates models that contain an SBML Intervention from Steady State (using SBML solver output)

Note that you can combine the calls if you wish, for example:
./ASPASIA.bash -le [full path to settings file] - creates models for Latin-Hypercube Analysis and eFAST (if specified correctly in the settings file)



--
Kieran Alden
York Computational Immunology Lab
February 2015
ycil@york.ac.uk

