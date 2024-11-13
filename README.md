# **Movecraft-Community Edition : Unteathered**


## Requires at least *Java 21+ & Minecraft 1.20.4-1.21.3*.
### **MUST** be using a Paper/Paper Fork (so like Purpur or something), We're ditching SpigotMC support with this one...




**New Mechanic-Features/Changes:**
 
- Fixes the creation of holes in the Water made by recently piloted & moving Ships.

- Redstone-Block Updates on moving crafts.

- Delayed the expensive (Both Server- and Client-side) Lighting Updates, by a few seconds (>5s) post movement. Should help with Player's FPS on large ships and makes Craft Movement visually smoother.

- New `config.yml` Options.
- New `ShipType.craft` Options.

- Improved how movecraft goes about block-setting and sending the changes to nearby players.

- Added back WASD Direct Control, use `/dc` to toggle it.

- Changes to how Fuel Items are burnt; Fuel may burn faster or slower compared to APDev/Mainstream Movecraft.

- New Total Blockcount Craft-HP Actionbar, displays the amount of Remaining blocks aboard the craft, over the amount of initial blocks. (Non-Air Blocks, in particular)

**API Changes:**
- Added new Fuel-type API/Event.
- TrackedLocations, used internally for various things, such as tracking Interior Air-Blocks aboard ships (Helps with mitigating the holes in the water), or keeping track of Fuel-Block Locations.
- Craft DataTags, Allow you to store arbitrary data "aboard" a Craft-Object. Is cleared upon the craft Releasing. Used for storing current Fly-Block & Move-Block count.
- The previously mentioned fuel item-changes, now allow you to register custom fuels via the CraftManager-API.
- SpeedModifier (SpeedMod) API, allowing developers to dynamically increase or decrease the speed of a given BaseCraft-Object.
  - Positive Numbers Decrease Speed/Increase delay of Movements; Negative Numbers do the opposite.

### Many *many* more changes, read the code to get a better idea...



## >>> EXPERIMENTAL ADDITION <<<
> The Following **ONLY** works if you are using the Paper Fork of:
> https://github.com/SparklyPower/SparklyPaper
> 
> Enables:
> 
> Multiworld-Threading (TLDR; Large Crafts will move without extensively lagging the main thread of the server.)
> *In the* `config.yml`
> 
```IsMultithreaded: true/false```
## >>> EXPERIMENTAL ADDITION <<<



This is **another** maintained fork of Movecraft, which aims to maintain compatibility with APDev/Mainstream-Movecraft as well as provide Performance Changes & Fixes, API Upgrades, Ticking/Updating Redstone Components.

## DOWNLOAD

(Pre)Release Builds can be found on the [releases tab](https://github.com/ccorp2002/Movecraft-CE-Unteathered/releases).

Development builds will eventually be found under the [actions tab](https://github.com/ccorp2002/Movecraft-CE-Unteathered/actions?query=workflow%3A%22Java+CI%22). 

(Use at your own risk!)


#### Movecraft is released under the GNU General Public License V3. 

# The following Guide is Incomplete.


## Development Environment (Set-up Guide)

> Compiling this Version of Movecraft is a bit more involved due to the additional features & changes, specific to certain paper versions & forks.
> However, once the set-up process is complete, you will not need to repeat it again.
> 
### How To setup the development environment:
- Compile yourself a copy of PaperMC for each of the MC Versions this supports.
- You will need `1.20.4, 1.21.1, 1.21.3` at the time of writing this.
- This can take a while, and be confusing. Suggest checking the Readme-section for of PaperMC over here [PaperMC](https://github.com/PaperMC/Paper)


Once you have put your compiled "server-<\version>" jars in the `libs` folder, run the following to build Movecraft through the `maven` build tool.
```
mvn clean install
```
Compiled jars can be found in the `/target` directory.
