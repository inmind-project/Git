%1 - group #1
%r1 - group #1 with reflection, e.g. I am -> you are, You're -> I'm etc.

pattern - pattern in sentence
Say - say a sentence

conditions and patterns are required for the Say and Set to take affect. Only one line can be matched.
state equals "start" at the beginning, make sure to change the state.
state="finish" - finish what you are doing and then clear dialog and reset back to top level.
state="return" - clear dialog and reset back to top level and find a new match for current sentence.
callfun="XXX"     - call the given function with all information in dialog (requires double quotes).
no quotes in conditions or set will look for a variable with that name.
condition - can use either = or ==, both mean equality. Supports also <,<=,>,>=,!=. supports also +,-,*,/. Can also use matching groups from pattern. Cannot concatenate strings. Can use javascript as well. 

