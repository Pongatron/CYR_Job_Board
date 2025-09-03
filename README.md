This is a custom database viewing software created for the company my father is a part of at his request. 
This serves/served as a learning experience for me to practice my coding skills for developing a real application that a few real people use.

This software mimics a previous excel spreadsheet the company used to display current job information like work order numbers and due dates.
It uses Java Swing to create an interface that interacts with a PostgreSQL database on their server that stores all active and inactive jobs.
Previously when they deleted/finished a job it would be gone forever, but this new software never deletes old jobs and just hides them so they can be accessed another time if need be.
Previously only one person could edit the excel sheet at a time. This one uses a database which allows real time edits from multiple desktops simultaneously.
This software also has a bootstrapper that checks this github repo, every time it is opened, for any releases I publish and installs the new version.

This software and its documentation are not as professional as I'd like, but I wanted to get it working as quick as I could and now I have no time to make changes.

This is an example of what it looks like with random data:
<img width="2560" height="1390" alt="image" src="https://github.com/user-attachments/assets/31921561-281b-4f81-a6ab-fc7b4a4ffa14" />

Author: Daniel Polgun 
