# HOMELAB

A self-hosted homelab running on Kubernetes. It serves my personal site at **[jamesonkoonce.com](https://jamesonkoonce.com)**, along with a Spring Boot stats API, a Postgres database, and an uptime monitor. It's exposed to the internet through a Cloudflare Tunnel, and Vercel takes over automatically whenever the homelab is down.

I built it to learn Docker and Kubernetes hands-on. Everything here is declared in YAML and can be rebuilt from this repo.

## What's running

| Namespace | Workload | Kind | Replicas | Purpose |
|---|---|---|---|---|
| `personal-site` | `personal-site` | Deployment | 2 | Next.js personal website |
| `stats` | `stats-api` | Deployment | 1 | Spring Boot API: LeetCode, cluster and uptime stats |
| `stats` | `stats-db` | StatefulSet | 1 | Postgres 17, stores uptime history |
| `stats` | `uptime-checker` | CronJob | every minute | Checks the public site and records the result |
| `cloudflared` | `cloudflared` | Deployment | 2 | Cloudflare Tunnel connector (outbound only) |
| `kube-system` | Traefik, CoreDNS, ... | k3s built-ins | | Ingress controller, cluster DNS, storage, metrics |

**Stack:** k3s · Docker · Next.js · Node · Spring Boot · Postgres · Flyway · Cloudflare Tunnel · Cloudflare Workers