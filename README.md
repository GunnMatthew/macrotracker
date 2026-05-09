Testing Edit!
# MacroTracker

## General Overview
MacroTracker is a personal nutrition tracker.  Specifically made for making counting macros, rather than calories, easier.  Users can add individual ingrediants or even create custom meals to save for future use in tracking their consumed foods.  MacroTracker will automatically calculate the Protein, Carbohydrates, and Fats consumed based off of what the user says they have eaten for the day by creating and writing to a local database.  Built primarily using Java 17, Maven, and SQLite.

### Why is this program a pre-release?
At the moment, I have completed my quarterly goals for this program.  Development will be continued to add features specified below, as well as a makeover for the GUI, at some point.  Because of this, I do not want to call it a full release as there are still features I'd like to add before I call this a fully completed program.  However, as it currently stands, the program is in working order for a very basic means of daily tracking of macro intake.

### Current Features
- Add custom ingredients with specified nutritional information.
- Create custom meals from added ingredients so meals can be reused when eaten in the future.
- View individual foods and meals in a list to add to consumed foods.
- Automated daily macro resetting.
- Common measurement conversions.  Metric or imperial?  No problem.  This converts most commonly used measurements so you can input however you'd like.  If you buy 3 pounds of hamburger and cook 1 pound and 3 ounces, it'll handle all the calculations for you!  If you find an unsupported measurement, please let me know so I can add it accordingly!

### Future Features (In no particular order):
- Historical logging
  - Store data from beyond just "today" to keep track of weekly averages and allow for users to check previous months/weeks.
- Personal Accounts
  - Creation of personal accounts to allow for more custom user tracking.
  - Allow for personal details such as weight and height to estimate macros for entered goals.
- Goal Tracking
  - Allow users to input their user goals and track how they stack up to their goals throughout the day.
  - Integration with historical logging to compare against weekly averages or past goal completion.
- Installer package
  - Make the installation process more user friendly and familiar.  Install required dependancies rather than storing in seperate folder and running via .bat file.
- PDF Read In
  - A lot of restaurants will upload their menu and nutritional information on a downloadable PDF.  Long term, I'd like to be able to scan and scrape these so users can easily add full menus to their food databases.

## Installation
  1. Find the "MacroTracker_PreRelease" and download it.  The most current version can be found [here.](https://github.com/GunnMatthew/macrotracker/releases/tag/MacroTracker_PreRelease)
  2. Extract the .Zip to it's desired location.
  3. From within the newly extracted folder, run "macroTracker.bat" and the program will start.
