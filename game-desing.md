

How to best decompose the game?

Thinking about creating a game object that keeps track of which camel is where.

20 spaces to travel

An array of 0-5 maps to one of the 6/7 colors
An array of Strings maps to one of the 6/7 colors
Each camel is an object keeping track of where they are currently


What is the CamelUp interface?

--- Actions?
- bet
- roll dice
- global bet (end game winner camel/end game biggest loser camel)
- Setting traps 

-- Obervers?
- get camels
- get dice
- get bets

Betting
Each camel (in the race) has 4 betting tags
- 1 five coin
- 1 three coin
- 2 two coin

When all tags for that camel are taken, they are out.

-- if the color matches
- if the camel is first, it is the value of the tag they own
- if the camel is second the owner gets 1 coin

-- otherwise
- if it is 3rd-5th place the owner loses a coin


Global Betting
- Each player has 5 global betting cards 1 for each camel
- Each player can choose to bet for the camel they think will win the whole game
- Each player can also choose to bet for the camel they think will be dead last
- The earlier you place your bet and are right the more you win.
    - Winners get 8, 5, 3, 2, 1, 0... 
    - So the first person to guess the right camel gets 1 coin
- You can place more than 1 camel in each pile, just know you only have 1 of each camel
    - You cannot change where you placed your color if you already placed it