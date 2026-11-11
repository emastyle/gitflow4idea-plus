# Changelog

Changes specific to the emastyle fork. Earlier upstream release notes are
available in the plugin's change notes.

## 0.8.2-emastyle.5

- Fix the "parent has already been disposed" error raised when a Git
  repository is closed while the Gitflow widget refreshes.
- Register repository cleanup only once instead of on every refresh.

## 0.8.2-emastyle.4

- Support PhpStorm 2026.3 (build 263), including its task platform dependency.
- Require a local PhpStorm 2026.3 installation when building the plugin.
- For PhpStorm 2026.2, use `0.8.2-emastyle.3`.

## 0.8.2-emastyle.3

- Declare the Gitflow settings page display name to avoid an IDE error.
- Prevent duplicate generated dialog methods in incremental builds.

## 0.8.2-emastyle.2

- Reduce Gitflow configuration reads and refresh the status widget in the
  background to avoid a startup UI freeze.

## 0.8.2-emastyle.1

- Update the plugin for PhpStorm 2026.2 (build 262).
- Preserve the configurable Gitflow start and finish options.
