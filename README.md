# Dirty
Quick and dirty access to NBT data of items, blocks, and entities.

Javadocs: [startux.github.io/Dirty](https://startux.github.io/Dirty/)

## Commands
All commands serve the purpose to dump NBT via chat or console in JSON
format.  They may only be issued via player.

* `/dirty item [options]` Inspect the item in your hand.
* `/dirty block [options]` Inspect a block.
* `/dirty entity [options]` Inspect an entity.
* `/dirty cancel` Cancel block or entity inspection.

Blocks and entities are chosen via right click right after the command
was issued.  The options are as follows.

* `console` Also print to console.
* `pretty` Pretty print the JSON result.