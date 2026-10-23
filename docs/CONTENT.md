# Guide content

Editorial content lives in `app/src/main/assets/guide.json`. The app includes
source links on articles, place details and bottle suggestions. Sources were
reviewed on 2026-10-23; this is a bundled edition, not a live availability feed.

Recommendations and the seven-day sequence are editorial choices. Producer
websites, Visit Madeira, IVBAM and the regional rum directory support production
facts, visit options and bottle names. Taberna da Poncha additionally uses the
All About Portugal venue listing; Casa do Povo da Serra de Água documents the
community's poncha festival. Do not describe these places as undiscovered or
claim a local consensus about the best drink.

When updating content:

- Verify the source page and revise the review date alongside changed facts.
- Keep visit facts distinct from suggestions about what to ask for.
- Avoid fixed prices, opening hours or promises of stock without current checks.
- Preserve the distinction between fortified Madeira Wine and Madeiran still wine.
- Update related day plans and bottle suggestions when changing a venue.
- Keep `id` values stable: saved places use those keys in local preferences.

## Map

`coastline.json` contains the Madeira polygon extracted from Natural Earth's
1:10m admin-0 country boundaries, Portugal feature. Original data:
https://github.com/nvkelso/natural-earth-vector/blob/master/geojson/ne_10m_admin_0_countries.geojson

Natural Earth data is public domain:
https://www.naturalearthdata.com/about/terms-of-use/

Place coordinates are approximate area markers. The UI explicitly says so.
They are not used for navigation; Google Maps receives venue names and addresses.
The coastline is an island overview rather than a street map. An accessible
numbered list mirrors every filtered pin, and overlapping pins offer a chooser.

The map, filters, content and saved places work without network access. Google
Maps, booking pages and source links open in an external application. The app
itself requests neither internet nor location permission.

## Visual browsing

Since 0.3.0, the main tabs present imagery and short captions. Full articles remain
in `guide.json` and `guide-de.json` and are shown on dedicated screens;
place tips, source links and itinerary transport notes are retained in details.
`media.json` holds photograph credits and curated video URLs. See [Media](MEDIA.md).

## Wine colours

`wineColors` supplies the red, white and rosé pages. Each bottle's `family` is one
of `Red`, `White`, `Rosé`, `Madeira Wine` or `Rum`; `style` further separates the
fortified Madeira Wine recommendations. Never assign a fortified Sercial or
Verdelho to the white table-wine family simply because the grape is white.
Atlantis product details use the Madeira Wine Company producer page. Stock and
tasting availability remain requests to confirm, not promises. Colour photographs
are illustrative and are labelled as such in their credits.

## Languages and navigation

English and German ship offline. Keep `guide.json` / `guide-de.json`,
`media.json` / `media-de.json` and `ui-en.json` / `ui-de.json` in sync.
The UI dictionaries use the English phrase as a stable key. The `tr` helper
localizes visible UI copy; content comes from the selected language's dataset.
Do not translate IDs, JSON keys, `type`, `style`, `family`, wine-colour `name`,
URLs, file paths, coordinates, or place references. Keep array order identical:
day and bottle routes use indices. Original photograph titles, creator names,
licenses, venue names and street addresses remain as attributed.

The first launch follows the device language (German or English fallback).
The header flag switches immediately and stores the choice locally. Screen
history, filters, saved places and scroll offsets survive language changes;
screen history and offsets are also saved across activity recreation.
Detail screens use a Back button and Android Back. Related articles open their
own pages; there are no modal dialogs or expanding text sections.
External webpages and videos retain their original languages.

## System appearance

The app follows Android's system night mode without a separate preference.
`values/colors.xml` and `values-night/colors.xml` define the UI and map palettes.
`BaseAppTheme` also switches native widget styling and system-bar icon contrast.
Android recreates the activity on a theme change, using the existing saved-state
handling for the current page, filters and scroll position. Photographs and map
pin fills keep their original colours; category text has separate theme colours.
The Android 12+ splash uses the adaptive launcher icon on a system-themed background.

## Origin and local availability

Wine colour sections split `scope` into `Made on Madeira` and
`Available on Madeira` (wines from elsewhere listed by an island venue).
Rum and fortified Madeira Wine retain their Madeira origin. The scope selector
is preserved across activity recreation and language changes.

Checked 2026-10-23: additional local rosés use Grandes Escolhas' Barbusano 2022
review, Crafted Wine & Spirits' Ilha brand page, and António Maçanita's Rosé dos
Villões 2025 technical sheet. Each entry includes the corresponding source URL.
The current Reid’s Palace dining page links the William Restaurant wine list
used for six outside-origin wines. These are menu listings, not live stock or
promises of a tasting. The restaurant lists a different vintage of Rosé dos
Villões from the producer sheet; the guide states that explicitly.

`place` is optional for bottles. `enquiry` references a source for stockist
questions. `availabilitySource`, `venue`, `visitSource` and `mapsQuery` provide
venue-specific menu evidence and visit links. Do not invent a map stop or send
unrelated bottles to an existing producer. Keep source URLs and structural
fields identical in both languages. Preserve bottle array ordering.
