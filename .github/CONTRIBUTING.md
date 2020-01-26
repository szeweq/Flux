# Contributing on Flux

Any contributions and Pull Requests are welcome here. Thanks for taking time to contribute! :+1:

This is a set of guidelines for contributing to Flux. You can feel free to propose changes to this document in a pull request.

## Translations
Any localizations should be made in `/src/main/resources/generators/flux/lang` directory.
The order of keys should be based on original English translation.

## Models and textures
While making a new block, please check `templates` and `generators` directories first.
Many blocks have generated models.
If a new block has similar model structure as others, then add its name to a specific file.

## Code contributions
Code style is based on default IntelliJ IDEA formatting for Java with minor changes.
Just remember that tabs are used instead of spaces.

This repository contains `buildSrc` directory with code used by Gradle to generate mod resources.
There is code written in Kotlin. Please make sure to check if Flux has been assembled correctly by Gradle before creating a Pull Request in this directory.