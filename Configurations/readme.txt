first column (condition): conditions to be met (can be empty)
	- state equals "start" at the beginning, make sure to change the state.
	- can use either = or ==, both mean equality. Supports also <,<=,>,>=,!=. supports also +,-,*,/. Can also use matching groups from pattern. Cannot concatenate strings. Can use javascript as well. 
	- no quotes in conditions or set will look for a variable with that name.

second column (pattern): regular expression to be matched (and used) - cannot be empty, use .* to match everything
	- %1 - group #1 etc.
	- %r1 - group #1 with reflection, e.g. I am -> you are, You're -> I'm etc.
	- case insensitive
third column (Say): a sentence to be said to the user (will send a message Say^sentence to the user)
fourth column (Set): set state and other variables.
	- state="finish" - finish what you are doing and then clear dialog and reset back to top level.
	- state="return" - clear dialog and reset back to top level and find a new match for current sentence.
	- callfun="XXX"     - call the given function with all information in dialog (requires double quotes).
	
	
fifth column / optional (Additional Commands): send addtional commands to the user, such as Launch^com.android.calculator2


conditions and patterns are required for the Say and Set to take affect. Only one line can be matched.




