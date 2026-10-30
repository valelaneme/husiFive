    visible: Boolean,
    onCopy: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            softWrap = false,
            style = MaterialTheme.typography.bodySmall,
        )
        val text = when {
            address == null -> stringResource(Res.string.no_statistics)
            visible -> address
            else -> maskNetworkAddress(address)
        }
        // Takes the remaining width so a long IPv6 address wraps instead of squeezing the label.
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable(enabled = address != null) {
                    onCopy(address ?: return@clickable)
                }
                .padding(horizontal = 6.dp, vertical = 2.dp),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmallEmphasized,
        )
    }
}

@Composable
private fun HideableSectionTitle(
    title: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        SimpleIconButton(
            imageVector = vectorResource(
                if (visible) {
                    Res.drawable.visibility
                } else {
                    Res.drawable.visibility_off
                },
            ),
            contentDescription = stringResource(
                if (visible) {
                    Res.string.hide
                } else {
                    Res.string.show
                },
            ),
            onClick = onToggleVisible,
        )
    }
}

private fun maskNetworkAddress(address: String): String {
    return if (':' in address) {
        "*::*"
    } else {
        "*.*.*.*"
    }
}

@Composable
private fun ExitIPCard(
    state: IPProbeState,
    detectIP: () -> Unit,
    onCopy: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        detectIP()
    }

    ElevatedCard(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(Res.string.ip_test), style = MaterialTheme.typography.titleMedium)
            state.ip?.let { ip ->
                SelectionContainer {
                    val displayText = countryEmoji(state.countryCode)?.let { emoji ->
                        "$emoji $ip"
                    } ?: ip
                    Text(
                        text = displayText,
                        modifier = Modifier.clickable { onCopy(ip) },
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            state.error?.let { error ->
                Text(stringOrRes(error), color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = detectIP, enabled = !state.isDoing) {
                    Text(
                        stringResource(
                            if (state.isDoing) {
                                Res.string.loading
                            } else {
                                Res.string.ip_test_detect
                            },
                        ),
                    )
                }
            }
        }
    }
