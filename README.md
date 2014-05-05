Stoa
====
In Greek, a **stoa** can be defined as follows:

    A portico, usually a detached portico of considerable length, that is used as a promenade or meeting place. 

Our Stoa obviously isn't a walkway, but it *is* a work of art. We made it over a considerable amount of time, and the metaphor extends itself well over the intended use of a meeting place, and history of its development only feeds in to the idea that the parent project (Demigods RPG) has essentially become an RPG maker.

We believe in our flagship experience (Demigods RPG), and we have spent many restless nights since late 2012 working on it, perfecting it, refactoring it, and ultimatly learning how to make an RPG for Minecraft.

***Stoa is meant to be the public, detached, and considerably long public meeting place for RPG plugins in Bukkit.***

While our focus will always remain on Demigods RPG, we will work with anyone willing to submit pull requests to this project and/or our more general library CensoredLib (TODO ADD LINK LATER).

What Stoa Is
====

Stoa is effectivly the engine behind Demigods.  Most every concept (characters, structures, deities (character-classes), abilities, skills, and more) has its utility functions, data, persistence, and low-level logic handled in Stoa. Things like indivigul deities, abilities, skills, or structures are implementations upon the concepts handled in Stoa.

This makes Demigods a sort of "plugin" for Stoa.

What Stoa Isn't
====

Stoa isn't a plugin. Stoa won't work on it's own. Stoa ins't something an Administrator can just install on their Bukkit server and expect to do something.

Stoa has no implementation built in, the details are left up to the developers. We recommend that developers shade Stoa into their plugins (with the maven-shade-plugin), relocating them (to prevent version mismatch problems), and basically allowing it to do the work in the shadows.

Stoa is NOT meant to be run in more than once instance. Plugins that take advantage of Stoa should be considered standalone, or should be components for another implementation of Stoa.

***Stoa is meant to make it easier to create an entirely new gamemode for survival Minecraft.***

What We Won't Do
====

 - We won't write your plugin for you.
 - We won't hold your hand.
 - We won't tell you how to run your server.
 - We won't give you up.
 - We won't let you down.
 - We won't turn around and desert you.
 - We won't make you cry.
 - We won't say goodbye. 
 - We won't tell a lie and hurt you.
 - We won't not never sometimes when asked if you don't want to not have fun, not have fun.

Got it?
