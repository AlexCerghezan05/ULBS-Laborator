import { EmptyState } from '../components/AppShell';

// Lista de facturi provizorie (DS-p21-app-shell). Lista reală, DS-p21-invoice-list, are machetă proprie mai târziu.
export default function InvoiceListPage() {
  return (
    <>
      <h1>Facturi</h1>
      <EmptyState
        title="Lista facturilor va apărea aici"
        text="Aici vor fi facturile emise, cu filtre după client, perioadă și status. Ecranul este în lucru."
      />
    </>
  );
}
