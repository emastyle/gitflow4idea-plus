# Git Flow Integration Plus for Intellij

## emastyle fork: PhpStorm 2026.3

This version targets PhpStorm 2026.3 (build family `263`) and preserves the
original Gitflow options page. Disable other Gitflow plugins, including the
original Git Flow Integration Plus, before installing this fork: their action
IDs and services overlap.
For PhpStorm 2026.2, use the earlier `0.8.2-emastyle.3` distribution.

In **Settings > Gitflow > Release**, enable **Keep Remote** to preserve published
release branches after finish. **Keep Local** controls local branch retention
independently. Leave **Fetch from Origin** disabled if you do not want start and
finish to fetch first. These settings are saved per project.

Release start delegates to the installed Git Flow CLI; the plugin does not
add Gitflow Pilot's remote-release validation. With AVH, an existing local
release still blocks another release. Other CLI implementations can impose
different checks; disabling fetch does not disable their validation.

Build with JDK 25 (the runtime bundled with PhpStorm 2026.3 can be used):

```bash
export JAVA_HOME=/Applications/PhpStorm.app/Contents/jbr/Contents/Home
bash ./gradlew -PlocalIdePath=/Applications/PhpStorm.app buildPlugin test verifyPlugin
```

Set `localIdePath` to your installed PhpStorm 2026.3. Install the ZIP from
`build/distributions/` using **Settings > Plugins > Install Plugin from Disk**.
The fork has a distinct plugin ID, `emastyle.gitflow4idea-plus`.
The release regression tests use temporary local repositories and a local bare
remote, never a GitHub remote. They require Git Flow AVH on `PATH` (other
implementations skip this AVH-specific suite).

### Available @ [JetBrains Plugins Repository][1]


An intelliJ plugin providing a UI layer for git-flow, which in itself is a collection of Git extensions to provide high-level repository operations for Vincent [Driessen's branching model](http://nvie.com/git-model).

![screenshot](https://github.com/RubinCarter/gitflow4idea-fix/blob/develop/docs/img/gitflow.png)

## Companion agent skill (experimental)

Using an AI coding agent? Check out **[gitflow-cli-plus](https://github.com/RubinCarter/gitflow-cli-plus)** — an agent skill companion to this plugin, derived from a functional analysis of its semantics. It ships a `SKILL.md` that teaches agents the same git-flow operations (init / feature / release / hotfix / bugfix), backed by a small Go CLI binary, follows the AVH edition semantics and config layout, and recognizes repos initialized by git-flow-next through a read-only compatibility layer.

**Note: this skill project is currently experimental** — expect rough edges, and please report anything you find in its [issue tracker](https://github.com/RubinCarter/gitflow-cli-plus/issues).

## Getting started

For the best introduction to get started with `git flow`, please read Jeff Kreeftmeijer's blog post:

[http://jeffkreeftmeijer.com/2010/why-arent-you-using-git-flow/](http://jeffkreeftmeijer.com/2010/why-arent-you-using-git-flow/)

Or have a look at this [cheat sheet](http://danielkummer.github.io/git-flow-cheatsheet/) by Daniel Kummer:

Huge shoutout [to Kirill Likhodedov](https://github.com/klikh), who wrote much of the original git4idea plugin, without which this plugin could not exist

## Online Installation

The plugin is available via the IntelliJ plugin manager. Just search for "Git Flow Integration Plus" to get the latest version!

**The plugin requires that you have gitflow installed, specifically the [AVH edition](https://github.com/petervanderdoes/gitflow). This is because the [Vanilla Git Flow](https://github.com/nvie/gitflow) hasn't been maintained in years.** See this page [for details](https://github.com/RubinCarter/gitflow4idea-fix/blob/develop/GITFLOW_VERSION.md)

## Offline Installation
download path: https://github.com/RubinCarter/gitflow4idea-fix/releases

Installation document:https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk

**The plugin requires that you have gitflow installed, specifically the [AVH edition](https://github.com/petervanderdoes/gitflow). This is because the [Vanilla Git Flow](https://github.com/nvie/gitflow) hasn't been maintained in years.** See this page [for details](https://github.com/RubinCarter/gitflow4idea-fix/blob/develop/GITFLOW_VERSION.md)

## Caveats

While the plugin is operational and contains all basic functions (init/feature/release/hotfix), it may contains bugs. With your help I'll be able to find and zap them all.

## Helping out

This project is under active development.
If you encounter any bug or an issue, I encourage you to add the them to the [Issues list](https://github.com/RubinCarter/gitflow4idea-fix/issues) on Github.
Feedback and suggestions are also very welcome.

## License

This plugin is under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
Copyright 2013-2020, Opher Vishnia.

## Who and why

This plugin was created by [Opher Vishnia](http://www.opherv.com), after I couldn't find any similar implementation.
I saw this [suggestion page](http://youtrack.jetbrains.com/issue/IDEA-65491) on the JetBrains site has more than 220 likes and 80 comments, and decided to take up the gauntlet :)

This plugin is forked from original https://github.com/OpherV/gitflow4idea .



[1]: https://plugins.jetbrains.com/plugin/18320-git-flow-integration-plus
