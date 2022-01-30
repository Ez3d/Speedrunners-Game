# SPEEDRUNNERS
Edward Zhou

2021-01-27

![image](https://user-images.githubusercontent.com/73318619/151687229-cac9bf8a-def1-4201-b3c8-d7976ed85177.png)


![helpImage](https://user-images.githubusercontent.com/73318619/151687137-ae8b3ce0-bc25-4e0d-ac41-12bba1476867.png)


WHAT'S THE GOAL?
    You are a speedrunner/parkourist/runner/racer, and you want to complete a lap (or however many laps you like) 
    of a course as fast as you can. You have many ways to move and manuver, so make use of them!
    The timer starts when you are loaded into the map. Finish the race and record your time by touching the checkered flag.

THE CHARACTER CAN:

    - Double jump (jump an additional time while in the air)
    - Wall jump (jump an additional time when colliding with a wall, as if vaulting off it)
    - Shoot their grappling hook at a 45 degree angle in the direction they are facing
    - Show off the cool animations I spent too much time making


![image](https://user-images.githubusercontent.com/73318619/151687252-2e5bce85-bd5b-4789-956e-82cb2123c68f.png)


ADDITIONAL NOTES:

    - Speed boost pads don't launch the player--they increase max velocity for a limited time. The direction the point is purely
        aesthetic
    - Solid white surfaces are hookable surfaces. Dashed white walls are climable walls
    - Pressing H toggles hitboxes if you ever need to see them
    - There is no loss in energy when swinging from the grappling hook, so holding down G will let you swing indefinitely
        This isn't a bug. It was left out because it has little impact on the game 
        (in a race against time you don't need to hang from your hook)
    - Pressing ESCAPE pauses the game or is a substitute for back in the menus

KNOWN BUGS/ISSUES:

    - When colliding with a ramp while grappling in a very specific way, an error occurs and the player is teleported to 
        the corner of the map. This is a very rare case and there wasn't a clear solution, so I left it.
    - At times, the player's hitbox does not line up well with the player model. This may make it seem like the player is
        partially in a wall, but it doesn't affect gameplay and is purely a visual thing. It also only happens rarely for very
        short frames (i.e. the peak of a grappling hook movement)
