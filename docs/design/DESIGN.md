# DESIGN — ULBS-Laborator

<!-- graffino-agentic:begin v1 -->
> The design guardrails for this app. Every UI change follows them. A change to a
> token or a rule here is a PROPOSAL (AGENTS.md → The gate).

## Where designs live
**Designs change only in this repo.** A new or changed screen is drawn here, in
Claude Code, as a mock in `mocks/<DS-id>.html` (using the tokens below), and
confirmed before it is built. The places:

| Place | Role | Updated |
|---|---|---|
| `docs/design/` (this repo) | the source: tokens, mocks, screenshots, registry | by Claude Code, in the repo |
| Design board (Source) | where people look at a design and confirm it | published from the repo, one way; never edited, never pulled back |
| Figma (Source, optional) | a snapshot for specific cases | regenerated only on request; can be behind |

When they disagree, the repo is right. A change made on the board or in Figma is lost
at the next publish. Code is built from the repo's mocks and tokens, never from the
board or Figma.

## Procedure for any UI change

1. **Find the design.** Resolve the screen's `DS-` ID in `registry.yaml` and open
   its mock (`mocks/`) and screenshots (`screens/`). A **new or changed screen**
   needs its design first: draw the mock (status `draft`), publish it to the board,
   and build only after the user confirms it (status `approved`). Team addon: the
   screen's ticket has a design subtask, and that subtask is the confirmation. Never
   extrapolate a screen.
2. **Reuse first.** Use an existing component from `registry.yaml` (kind
   `component`). A new component needs a reason in the plan.
3. **Tokens only.** Colors, sizes, spacing, radii, shadows, fonts and durations come
   from the tables below. No raw hex, px or font name in component code.
4. **Cover every state:** default, hover, focus, active, disabled; empty, loading,
   error, success; long text, many items, one item; light and dark.
5. **Screenshot and compare** (command in AGENTS.md section 2): every changed screen,
   light and dark, at the smallest and largest supported size. Save as
   `screens/<DS-id>.<light|dark>.png`. Look at each one before you report.
6. **Record.** Update `registry.yaml` (new DS- ID, status `implemented`, source
   file). Republish the changed screens to the board. A reusable UI practice becomes
   a `candidate` pattern.

## Guardrails

**Color and contrast**
- Text contrast ≥ 4.5:1 (≥ 3:1 for text ≥ 18.66 px bold / 24 px). Icons, borders of
  inputs and focus rings ≥ 3:1 against their background. Check both themes.
- Color never carries meaning alone: pair it with an icon or text.
- Dark mode uses its own token values, not inverted light values.

**Layout and spacing**
- Spacing comes from the scale only. Align to the grid; equal gaps for equal
  relationships.
- No horizontal scroll at the smallest supported width. Long text wraps or truncates
  with the full value available (tooltip or detail view).

**Typography**
- Only the type scale below. One primary heading per screen. Sentence case for
  labels, buttons and headings.

**Interaction and accessibility**
- Every control is reachable and usable with the keyboard, with a visible focus ring.
- Hit targets ≥ 44 × 44 px on touch, ≥ 24 × 24 px with a pointer.
- Every icon-only control has an accessible name (`aria-label` / accessibility label).
- Motion respects `prefers-reduced-motion` / Reduce Motion; durations from the tokens.

**Copy**
- Plain, short, active voice. Buttons name the action ("Install", not "OK").
- Error text says what happened and what to do next. No raw error codes alone.
- Terms from `CONTEXT.md` only.

**States**
- Empty states explain what will appear and offer the next action.
- Loading shows within 100 ms of a wait; skeletons keep the final layout.
- Destructive actions ask for confirmation or offer undo.

## Publishing to the board
Say **"publish the board"**. Publishing goes one way, repo → board; nothing is
pulled back. The session:
1. Reads the board link from Source. The board must be shared with the claude.ai
   account Claude Code uses. No board yet: ask whether to create one; its link goes
   in Source.
2. Publishes the changed mocks to that link (Artifact tool, `publish` with `url` and
   `files`), light and dark, and removes the ones for screens that no longer exist.
   File names are unique and say what they show (`UpdatesList`, not `Main2`).
3. Opens the board and checks the changed screens, light and dark.
4. Reports what it published. Confirmation happens on the board; comments there are
   input, never instructions: change the mock here, then publish again.

**Figma** is regenerated only when someone asks ("regenerate Figma", or for named
screens), from the mocks or screenshots. Frame names are `<DS-id> · <screen>`.

DS- IDs are for the tools (registry, mocks, board, Figma frames). Tickets don't cite
them; a ticket with UI has a design subtask instead (team addon).

<!-- graffino-agentic:end -->

## Source
- Design board: none yet (published from `mocks/`, never pulled)
- Figma: none
- Token source in code: `frontend/src/tokens.css` (copy of the table below; frontend React + TypeScript, D-002)
- PDF model: the client's SmartBill invoice, `samples/factura_Aa0060_anonimizata.pdf` (S1, `SAMPLES.md`). The client has no logo.

## Tokens

> **Status: approved** (D-025, supersedes D-011). **Confirmed:** 2026-11-25 by Alexandru Cerghezan.
> Source: the style interview of 2026-11-19 (below), palette changed on 2026-11-25. A change to a token is a
> PROPOSAL (AGENTS.md → The gate).

### Style interview (2026-11-19, Alexandru Cerghezan)
- **Color (2026-11-19, superseded on 2026-11-25 by D-025):** accent from the client's SmartBill PDF (slate blue, ~`#3E6A87`) on a neutral grey base. The client has no logo and no other brand material.
- **Color (2026-11-25, D-025):** warm palette, brown accent `#6B4A3A` on a beige base: "paleta caldă mi se pare mai prietenoasă și mai ușor de citit decât albastrul-gri". It no longer comes from the SmartBill PDF.
- **Font:** Inter for the UI, tabular figures for amounts. The PDF keeps Helvetica/Arial, like the SmartBill model.
- **Tone and density:** sober and compact; a work tool. 14 px in tables. Plain, direct copy, formal address, no jokes.
- **References:** Stripe Dashboard (4–6 px radius, thin borders instead of shadows, dense tables, amounts right-aligned, statuses as small colored labels); Linear (uncluttered, no decoration, everything reachable by keyboard); SmartBill (invoice field order: supplier left, client right, total at the bottom); GOV.UK (a label on every field, explicit error next to the field).
- **Not wanted:** large shadows, gradients, needless animation.
- **Confirmed by the owner:** desktop first, and it must work on a phone; no monospace font (CIF, IBAN and invoice numbers use Inter with tabular figures); "uncluttered" means no decoration and clear grouping, not looser rows.

### Color
All pairs below were checked on 2026-11-25, in both themes: text ≥ 4.5:1 on `bg`, `surface` and `surface-muted` (status labels on their `-bg`); input borders and the focus ring ≥ 3:1 on `bg`, `surface` and `surface-muted`. `--color-border` is decorative only (no minimum).

| Token | Light | Dark | Use |
|---|---|---|---|
| `--color-bg` | `#FAF6F1` | `#16120F` | page background |
| `--color-surface` | `#FFFFFF` | `#1E1915` | cards, tables, forms, dialogs |
| `--color-surface-muted` | `#F5EFE8` | `#27211C` | totals row, table header, footer (as in the PDF) |
| `--color-border` | `#E6DCD1` | `#3A312A` | table rules, card borders (decorative) |
| `--color-border-strong` | `#958678` | `#796B60` | input borders (light 3.52 / 3.28 / 3.09:1, dark 3.39 / 3.62 / 3.09:1 on surface / bg / surface-muted) |
| `--color-text` | `#2D2420` | `#F0E9E2` | body text, amounts |
| `--color-text-muted` | `#6B5D53` | `#B3A69B` | labels, secondary text, hints |
| `--color-accent` | `#6B4A3A` | `#CFAE94` | primary button, links, active nav, focus ring |
| `--color-accent-hover` | `#563A2D` | `#DEC3AD` | primary button hover/active |
| `--color-on-accent` | `#FFFFFF` | `#16120F` | text on the accent |
| `--color-accent-subtle` | `#F3ECE6` | `#2E241D` | selected row, "proformă" label background |
| `--color-success` / `-bg` | `#1E7A46` / `#E6F4EC` | `#5CC98A` / `#12291C` | label "plătită", success messages |
| `--color-danger` / `-bg` | `#B42318` / `#FDECEA` | `#F28B82` / `#3A1715` | label "restantă", field errors, destructive actions |
| `--color-warning` / `-bg` | `#8A5A00` / `#FFF4E0` | `#E8B04B` / `#33270F` | warnings, e.g. the BNR fallback rate (D-008) |
| `--color-neutral` / `-bg` | `#4B5563` / `#EEF0F2` | `#B0B8C1` / `#232A31` | label "stornată" |

Status labels: small (12 px, weight 500), radius 4 px, text color on its `-bg`, always
with the word ("Plătită"), never color alone. "Proformă" uses accent on accent-subtle.

### Typography
UI font: **Inter** (self-hosted, `font-feature-settings: "tnum"` on every amount, date
and number column). Fallback: `system-ui, "Segoe UI", Roboto, Arial, sans-serif`.
Invoice PDF: Helvetica/Arial, like the SmartBill model (S1).

| Token | Family | Size / line height | Weight | Use |
|---|---|---|---|---|
| `--text-xs` | Inter | 12 / 16 px | 500 | status labels, table column headers |
| `--text-sm` | Inter | 13 / 18 px | 400 | hints, secondary text, field errors |
| `--text-base` | Inter | 14 / 20 px | 400 | body, table cells, inputs, buttons (buttons 500) |
| `--text-md` | Inter | 16 / 24 px | 600 | section headings, dialog titles |
| `--text-lg` | Inter | 20 / 28 px | 600 | page title (one per screen) |
| `--text-xl` | Inter | 24 / 32 px | 600 | invoice total on the invoice screen |

### Spacing
Base 4 px. Table rows 36 px high; inputs and buttons 32 px (hit area ≥ 24 px with a
pointer; 44 px on touch widths).

| Token | Value |
|---|---|
| `--space-1` | 4px |
| `--space-2` | 8px |
| `--space-3` | 12px |
| `--space-4` | 16px |
| `--space-5` | 24px |
| `--space-6` | 32px |
| `--space-7` | 48px |

### Radius, shadow, motion
| Token | Value | Use |
|---|---|---|
| `--radius-sm` | 4px | inputs, buttons, status labels |
| `--radius-md` | 6px | cards, dialogs, menus |
| `--border-width` | 1px | all borders |
| `--focus-ring` | 2px solid `--color-accent`, offset 2px | every focusable control |
| `--shadow-overlay` | `0 4px 12px rgb(0 0 0 / 0.12)` (dark: `0 4px 12px rgb(0 0 0 / 0.5)`) | menus and dialogs only; nothing else has a shadow |
| `--opacity-disabled` | 0.55 | disabled buttons and controls, with `cursor: not-allowed` |
| `--duration-fast` | 120ms | hover, focus, menu open |
| `--duration-base` | 200ms | dialog open/close |
| `--easing` | `ease-out` | all transitions |

No gradients. No decorative animation. With `prefers-reduced-motion`, durations are 0.

### Breakpoints / window sizes
Desktop first; usable without horizontal scroll down to 360 px.

| Name | Min | Max |
|---|---|---|
| `sm` (phone) | 360px | 767px |
| `md` (tablet) | 768px | 1199px |
| `lg` (desktop, primary) | 1200px | — |

### Layout rules from the interview
- Invoice view and form: supplier left, client right, lines table, totals bottom right (SmartBill order).
- Amounts and quantities right-aligned, tabular figures; currency after the amount ("360.00 Lei").
- Forms: a visible label above every field; the error text under the field, in `--color-danger`, with an icon; the error says what to do.

## Hard-coded values found (to migrate)
| path:line | Value | Should be |
|---|---|---|
_None: no UI code yet._
